package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.Paginacion;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
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
                            .flatMap(this::complementarSolicitud)
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

    private Mono<DetallesSolicitudes> complementarSolicitud(Solicitud solicitud){
        Mono<String> monoEstado = estadoRepository.obtenerNombreEstadoPorId(solicitud.getEstadoId());
        Mono<TipoPrestamo> monoTipoPrestamo = tipoPrestamoRepository.obtenerPorId(solicitud.getTipoPrestamoId());
        Mono<Usuario> monoUsuario = usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(solicitud.getDocumentoId());
        return Mono.zip(
                Mono.just(solicitud),
                monoEstado,
                monoTipoPrestamo,
                monoUsuario
        ).map(this::unificarResultados);
    }

    private DetallesSolicitudes unificarResultados(Tuple4<Solicitud, String, TipoPrestamo, Usuario> tupla4){
        Solicitud solicitud = tupla4.getT1();
        String nombreEstado = tupla4.getT2();
        TipoPrestamo tipoPrestamo = tupla4.getT3();
        Usuario usuario = tupla4.getT4();

        return DetallesSolicitudes.builder()
                .documentoId(solicitud.getDocumentoId())
                .correoElectronico(usuario.getCorreoElectronico())
                .salarioBase(usuario.getSalarioBase())
                .nombresUsuario(usuario.getNombres())
                .apellidosUsuario(usuario.getApellidos())
                .monto(solicitud.getMonto())
                .plazo(solicitud.getPlazo())
                .estadoSolicitud(nombreEstado)
                .tipoPrestamo(tipoPrestamo.getNombre())
                .tasaInteresPrestamo(tipoPrestamo.getTasaInteres())
                .deudaTotalMensualSolicitudesAprobadas(BigDecimal.ZERO)
                .build();
    }

}
