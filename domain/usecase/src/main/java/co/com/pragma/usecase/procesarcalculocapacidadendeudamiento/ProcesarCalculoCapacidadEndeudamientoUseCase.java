package co.com.pragma.usecase.procesarcalculocapacidadendeudamiento;

import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.mensaje.gateways.MensajeUtilsGateway;
import co.com.pragma.model.solicitud.EstadoSolicitud;
import co.com.pragma.model.solicitud.MensajeProcesadoSolicitud;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class ProcesarCalculoCapacidadEndeudamientoUseCase {

    private final MensajeUtilsGateway mensajeUtilsGateway;
    private final SolicitudRepository solicitudRepository;
    private final MensajeSQSGateway mensajeSQSGateway;

    public Mono<Solicitud> ejecutar(String messageBody){
        return mensajeUtilsGateway.deserializarMensaje(messageBody, MensajeProcesadoSolicitud.class)
                .flatMap(mensaje ->
                        solicitudRepository.obtenerSolicitudPorId((long) mensaje.getSolicitudId())
                                .flatMap(solicitud -> {
                                    solicitud.setEstadoId(EstadoSolicitud.getIdByNombre(mensaje.getEstado()));
                                    solicitud.setFechaAprobacion(LocalDateTime.now());
                                    return validarEstado(solicitud, mensaje)
                                            .flatMap(solicitudRepository::guardar)
                                            .flatMap(solicitudGuardada -> EstadoSolicitud.APROBADA.getId().equals(solicitudGuardada.getEstadoId())
                                                    ? mensajeSQSGateway.enviarSolicitudAprobada(solicitudGuardada)
                                                    : Mono.just(solicitudGuardada));
                                })
                );
    }

    private Mono<Solicitud> validarEstado(Solicitud solicitud, MensajeProcesadoSolicitud mensaje) {
        return Mono.just(solicitud)
                .map(s -> {
                    s.setEstadoId(EstadoSolicitud.getIdByNombre(mensaje.getEstado()));
                    s.setFechaAprobacion(
                            EstadoSolicitud.APROBADA.getId().equals(s.getEstadoId())
                                    ? ZonedDateTime.now(ZoneId.of("America/Bogota")).toLocalDateTime()
                                    : s.getFechaAprobacion()
                    );
                    return s;
                });
    }

}
