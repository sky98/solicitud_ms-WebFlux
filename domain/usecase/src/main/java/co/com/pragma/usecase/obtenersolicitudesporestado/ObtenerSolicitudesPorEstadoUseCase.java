package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.Paginacion;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.SolicitudResponse;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ObtenerSolicitudesPorEstadoUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final UsuarioResConsumerGateway usuarioRestConsumerGateway;

    public Mono<Paginacion<DetallesSolicitudes>> ejecutar(Integer estadoId, Integer limit, Integer offset) {
        return validarEstado(estadoId)
                .flatMap(estado -> {

                    Mono<List<DetallesSolicitudes>> solicitudesMono = obtenerSolicitudesAgrupadasYEnriquecidas(Mono.just(estado), estadoId, limit, offset);
                    Mono<Long> totalElementosMono = solicitudRepository.contarSolicitudesPorEstado(estadoId);

                    return Mono.zip(solicitudesMono, totalElementosMono)
                            .map(tupla -> construirPaginacion(tupla.getT1(), tupla.getT2(), limit, offset));
                });
    }

    private Mono<Estado> validarEstado(Integer estadoId) {
        return estadoRepository.obtenerPorId(Long.valueOf(estadoId))
                .switchIfEmpty(Mono.error(new ErrorValidacion("Estado no encontrado", Set.of("estadoId : " + estadoId))));
    }

    private Mono<List<DetallesSolicitudes>> obtenerSolicitudesAgrupadasYEnriquecidas(Mono<Estado> estadoMono,Integer estadoId, Integer limit, Integer offset) {
        return solicitudRepository.obtenerSolicitudesPorEstado(estadoId, limit, offset)
                .groupBy(Solicitud::getDocumentoId)
                .flatMap(groupFlux ->
                        groupFlux.collectList()
                                .flatMap(solicitudesDelGrupo -> {
                                    if (solicitudesDelGrupo.isEmpty()) {
                                        return Mono.empty();
                                    }
                                    Solicitud primeraSolicitud = solicitudesDelGrupo.get(0);
                                    Mono<TipoPrestamo> monoTipoPrestamo = tipoPrestamoRepository.obtenerPorId(primeraSolicitud.getTipoPrestamoId()).cache();
                                    Mono<Usuario> monoUsuario = usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(groupFlux.key()).cache();

                                    return Mono.zip(
                                            Mono.just(solicitudesDelGrupo),
                                            estadoMono,
                                            monoTipoPrestamo,
                                            monoUsuario
                                    ).map(this::construirDetallesSolicitudes);
                                })
                )
                .collectList();
    }

    private DetallesSolicitudes construirDetallesSolicitudes(Tuple4<List<Solicitud>, Estado, TipoPrestamo, Usuario> tupla) {
        List<Solicitud> solicitudes = tupla.getT1();
        Estado estado = tupla.getT2();
        TipoPrestamo tipoPrestamo = tupla.getT3();
        Usuario usuario = tupla.getT4();

        List<SolicitudResponse> solicitudesResponse = solicitudes.stream()
                .map(solicitud -> SolicitudResponse.builder()
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
                .deudaTotalMensualSolicitudesAprobadas(calcularDeudaMensual(solicitudes))
                .build();
    }

    private Paginacion<DetallesSolicitudes> construirPaginacion(List<DetallesSolicitudes> solicitudes, long totalElementos, Integer limit, Integer offset) {
        long totalPaginas = (long) Math.ceil((double) totalElementos / limit);

        return Paginacion.<DetallesSolicitudes>builder()
                .numeroPagina(offset / limit + 1)
                .tamanoPagina((long) limit)
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
