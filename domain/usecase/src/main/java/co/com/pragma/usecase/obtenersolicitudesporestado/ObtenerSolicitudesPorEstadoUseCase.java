package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.Paginacion;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.SolicitudesProcesadas;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ObtenerSolicitudesPorEstadoUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final UsuarioResConsumerGateway usuarioRestConsumerGateway;

    public Mono<Paginacion<DetallesSolicitudes>> ejecutar(Integer estadoId, Integer limit, Integer offset) {
        Mono<Estado> estadoMono = validarEstado(estadoId).cache();
        Mono<Long> totalElementosMono = solicitudRepository.contarSolicitudesPorEstado(estadoId);

        Mono<List<DetallesSolicitudes>> detallesSolicitudesMono = estadoMono.flatMap(estado ->
                solicitudRepository.obtenerSolicitudesPorEstado(estadoId, limit, offset)
                        .collectList()
                        .flatMap(solicitudes -> procesarSolicitudesYEnriquecerDatos(solicitudes, estado))
        );

        return Mono.zip(detallesSolicitudesMono, totalElementosMono)
                .map(tupla -> construirPaginacion(tupla.getT1(), tupla.getT2(), limit, offset));
    }

    private Mono<Estado> validarEstado(Integer estadoId) {
        return estadoRepository.obtenerPorId(estadoId.longValue())
                .switchIfEmpty(Mono.error(new ErrorValidacion("Estado no encontrado", Set.of("estadoId : " + estadoId))));
    }

    private Mono<List<DetallesSolicitudes>> procesarSolicitudesYEnriquecerDatos(List<Solicitud> solicitudes, Estado estado) {
        if (solicitudes.isEmpty()) {
            return Mono.just(List.of());
        }

        Set<Long> documentoIds = solicitudes.stream().map(Solicitud::getDocumentoId).collect(Collectors.toSet());
        Set<Long> tipoPrestamoIds = solicitudes.stream().map(Solicitud::getTipoPrestamoId).collect(Collectors.toSet());

        Mono<Map<Long, Usuario>> usuariosMapMono = obtenerUsuariosPorDocumentoIds(documentoIds);
        Mono<Map<Long, TipoPrestamo>> tipoPrestamosMapMono = obtenerTiposDePrestamoPorIds(tipoPrestamoIds);

        return Mono.zip(
                Mono.just(solicitudes),
                usuariosMapMono,
                tipoPrestamosMapMono
        ).map(tupla -> construirDetallesSolicitudes(tupla.getT1(), tupla.getT2(), tupla.getT3(), estado));
    }

    private Mono<Map<Long, Usuario>> obtenerUsuariosPorDocumentoIds(Set<Long> documentoIds) {
        return Flux.fromIterable(documentoIds)
                .flatMap(usuarioRestConsumerGateway::obtenerUsuarioPorDocumentoId)
                .collectMap(Usuario::getDocumentoId);
    }

    private Mono<Map<Long, TipoPrestamo>> obtenerTiposDePrestamoPorIds(Set<Long> tipoPrestamoIds) {
        return Flux.fromIterable(tipoPrestamoIds)
                .flatMap(tipoPrestamoRepository::obtenerPorId)
                .collectMap(TipoPrestamo::getTipoPrestamoId);
    }

    private List<DetallesSolicitudes> construirDetallesSolicitudes(List<Solicitud> solicitudes, Map<Long, Usuario> usuariosMap, Map<Long, TipoPrestamo> tipoPrestamosMap, Estado estado) {
        return solicitudes.stream()
                .collect(Collectors.groupingBy(Solicitud::getDocumentoId))
                .entrySet().stream()
                .map(entry -> {
                    List<Solicitud> solicitudesPorUsuario = entry.getValue();
                    Long documentoId = entry.getKey();
                    Solicitud primeraSolicitud = solicitudesPorUsuario.get(0);

                    Usuario usuario = usuariosMap.get(documentoId);
                    TipoPrestamo tipoPrestamo = tipoPrestamosMap.get(primeraSolicitud.getTipoPrestamoId());

                    List<SolicitudesProcesadas> solicitudesResponse = solicitudesPorUsuario.stream()
                            .map(solicitud -> SolicitudesProcesadas.builder()
                                    .solicitudId(solicitud.getSolicitudId())
                                    .monto(solicitud.getMonto())
                                    .plazo(solicitud.getPlazo())
                                    .estado(estado.getNombre())
                                    .tipoPrestamo(tipoPrestamo.getNombre())
                                    .tasaInteres(tipoPrestamo.getTasaInteres())
                                    .build())
                            .collect(Collectors.toList());

                    return DetallesSolicitudes.builder()
                            .documentoId(usuario.getDocumentoId())
                            .correoElectronico(usuario.getCorreoElectronico())
                            .salarioBase(usuario.getSalarioBase())
                            .nombresUsuario(usuario.getNombres())
                            .apellidosUsuario(usuario.getApellidos())
                            .solicitudes(solicitudesResponse)
                            .deudaTotalMensualSolicitudesAprobadas(calcularDeudaMensual(solicitudesPorUsuario))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Paginacion<DetallesSolicitudes> construirPaginacion(List<DetallesSolicitudes> solicitudes, long totalElementos, Integer limit, Integer offset) {
        long totalPaginas = (long) Math.ceil((double) totalElementos / limit);

        return Paginacion.<DetallesSolicitudes>builder()
                .numeroPagina(offset / limit + 1)
                .tamanoPagina(limit.longValue())
                .totalElementos(totalElementos)
                .totalPaginas(totalPaginas)
                .items(solicitudes)
                .build();
    }

    private BigDecimal calcularDeudaMensual(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .filter(s -> s.getEstadoId() == 4L)
                .map(Solicitud::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
