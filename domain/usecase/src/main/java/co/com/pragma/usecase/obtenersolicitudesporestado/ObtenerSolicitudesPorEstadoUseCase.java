package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.Paginacion;
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

    public Mono<Paginacion<DetallesSolicitudes>> ejecutar(Integer estadoId, Integer limit, Integer offset){
        return estadoRepository.existeEstadoPorId(Long.valueOf(estadoId))
                .filter(existe -> existe)
                .switchIfEmpty(Mono.error(new ErrorValidacion("Estado no encontrado", Set.of("estadoId"))))
                .flatMap(existe -> {

                    Mono<List<DetallesSolicitudes>> listaSolicitudesMono = solicitudRepository.obtenerSolicitudesPorEstado(estadoId, limit, offset)
                            .groupBy(Solicitud::getDocumentoId)
                            .flatMap(groupFlux -> {
                                Long documentoId = groupFlux.key();
                                return groupFlux.collectList()
                                        .flatMap(solicitudesDelGrupo -> {
                                            if (solicitudesDelGrupo.isEmpty()) {
                                                return Mono.empty();
                                            }
                                            Solicitud primeraSolicitud = solicitudesDelGrupo.get(0);
                                            Mono<String> monoEstado = estadoRepository.obtenerNombreEstadoPorId(primeraSolicitud.getEstadoId());
                                            Mono<TipoPrestamo> monoTipoPrestamo = tipoPrestamoRepository.obtenerPorId(primeraSolicitud.getTipoPrestamoId());
                                            Mono<Usuario> monoUsuario = usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(documentoId);

                                            return Mono.zip(
                                                    Mono.just(solicitudesDelGrupo),
                                                    monoEstado,
                                                    monoTipoPrestamo,
                                                    monoUsuario
                                            ).map(tupla -> {
                                                List<Solicitud> solicitudes = tupla.getT1();
                                                String nombreEstado = tupla.getT2();
                                                TipoPrestamo tipoPrestamo = tupla.getT3();
                                                Usuario usuario = tupla.getT4();

                                                List<SolicitudResponse> solicitudesResponse = solicitudes.stream()
                                                        .map(solicitud -> SolicitudResponse.builder()
                                                                .solicitudId(solicitud.getSolicitudId())
                                                                .monto(solicitud.getMonto())
                                                                .plazo(solicitud.getPlazo())
                                                                .estado(nombreEstado)
                                                                .tipoPrestamo(tipoPrestamo.getNombre())
                                                                .build())
                                                        .collect(Collectors.toList());

                                                return DetallesSolicitudes.builder()
                                                        .documentoId(documentoId)
                                                        .correoElectronico(usuario.getCorreoElectronico())
                                                        .salarioBase(usuario.getSalarioBase())
                                                        .nombresUsuario(usuario.getNombres())
                                                        .apellidosUsuario(usuario.getApellidos())
                                                        .solicitudes(solicitudesResponse)
                                                        .deudaTotalMensualSolicitudesAprobadas(BigDecimal.ZERO)
                                                        .build();
                                            });
                                        });
                            })
                            .collectList();

                    Mono<Long> totalElementosMono = solicitudRepository.contarSolicitudesPorEstado(estadoId);

                    return Mono.zip(listaSolicitudesMono, totalElementosMono)
                            .map(tupla ->{
                                List<DetallesSolicitudes> solicitudes = tupla.getT1();
                                long totalElementos = tupla.getT2();
                                long totalPaginas = (long) Math.ceil((double) totalElementos / limit);

                                return Paginacion.<DetallesSolicitudes>builder()
                                        .numeroPagina(offset / limit + 1)
                                        .tamanoPagina((long) limit)
                                        .totalElementos(totalElementos)
                                        .totalPaginas(totalPaginas)
                                        .items(solicitudes)
                                        .build();
                            });
                });
    }

}
