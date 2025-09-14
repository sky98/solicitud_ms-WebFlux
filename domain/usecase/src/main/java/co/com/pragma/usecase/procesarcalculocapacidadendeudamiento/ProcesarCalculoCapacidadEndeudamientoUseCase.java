package co.com.pragma.usecase.procesarcalculocapacidadendeudamiento;

import co.com.pragma.model.mensaje.gateways.MensajeUtilsGateway;
import co.com.pragma.model.solicitud.EstadoSolicitud;
import co.com.pragma.model.solicitud.MensajeProcesadoSolicitud;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ProcesarCalculoCapacidadEndeudamientoUseCase {

    private final MensajeUtilsGateway mensajeUtilsGateway;
    private final SolicitudRepository solicitudRepository;

    public Mono<Solicitud> ejecutar(String messageBody){
        return mensajeUtilsGateway.deserializarMensaje(messageBody, MensajeProcesadoSolicitud.class)
                .flatMap(mensaje ->
                        solicitudRepository.obtenerSolicitudPorId((long) mensaje.getSolicitudId())
                                .flatMap(solicitud -> {
                                    solicitud.setEstadoId(EstadoSolicitud.getIdByNombre(mensaje.getEstado()));
                                    return solicitudRepository.guardar(solicitud);
                                })
                );
    }

}
