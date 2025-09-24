package co.com.pragma.usecase.actualizarestadosolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.EstadoSolicitud;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

@RequiredArgsConstructor
public class ActualizarEstadoSolicitudUseCase {

    private static final String ESTADO_APROBADO = "Aprobada";
    private static final String ESTADO_RECHAZADA = "Rechazada";

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final MensajeSQSGateway mensajeSQSGateway;

    public Mono<Solicitud> ejecutar(Solicitud solicitud){
        return solicitudRepository.obtenerSolicitudPorId(solicitud.getSolicitudId())
                .switchIfEmpty(Mono.error(new ErrorDominio("Solicitud con id : " + solicitud.getSolicitudId() + ", no encontrada", Set.of("solicitudId:"+solicitud.getSolicitudId()))))
                .flatMap(solicitudEncontrada -> estadoRepository.obtenerPorId(solicitud.getEstadoId())
                        .switchIfEmpty(Mono.error(new ErrorDominio("Estado a actualizar no es el correcto", Set.of(solicitud.getEstadoId().toString()))))
                        .filter(estado -> ESTADO_APROBADO.equals(estado.getNombre()) || ESTADO_RECHAZADA.equals(estado.getNombre()))
                        .switchIfEmpty(Mono.error(new ErrorDominio("Estado a actualizar no es valido (Aprobada, Rechazada)", Set.of("estadoId:"+solicitud.getEstadoId()))))
                        .flatMap(estado -> validarEstado(solicitudEncontrada, estado)
                                .flatMap(solicitudRepository::guardar)
                                .flatMap(solicitudGuardada -> mensajeSQSGateway.enviarSolicitudActualizada(solicitudGuardada)
                                        .onErrorResume(e -> solicitudRepository.rollback(solicitudEncontrada))
                                        .flatMap(solicitudEnviada -> ESTADO_APROBADO.equals(estado.getNombre())
                                                    ? mensajeSQSGateway.enviarSolicitudAprobada(solicitudEnviada)
                                                    : Mono.just(solicitud)
                                        )
                                        .thenReturn(solicitudGuardada)
                                ))
                );
    }

    private Mono<Solicitud> validarEstado(Solicitud solicitud, Estado estado) {
        return Mono.just(solicitud)
                .map(s -> {
                    s.setEstadoId(EstadoSolicitud.getIdByNombre(estado.getNombre()));
                    s.setFechaAprobacion(
                            EstadoSolicitud.APROBADA.getId().equals(estado.getEstadoId())
                                    ? ZonedDateTime.now(ZoneId.of("America/Bogota")).toLocalDateTime()
                                    : s.getFechaAprobacion()
                    );
                    return s;
                });
    }

}
