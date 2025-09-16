package co.com.pragma.usecase.actualizarestadosolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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
                        .flatMap(estado -> {
                            solicitudEncontrada.setEstadoId(estado.getEstadoId());
                            return solicitudRepository.guardar(solicitudEncontrada)
                                    .flatMap(solicitudGuardada -> mensajeSQSGateway.enviarSolicitudActualizada(solicitudGuardada)
                                            .onErrorResume(e -> solicitudRepository.rollback(solicitudEncontrada))
                                            .thenReturn(solicitudGuardada)
                                    );
                        })
                );
    }

}
