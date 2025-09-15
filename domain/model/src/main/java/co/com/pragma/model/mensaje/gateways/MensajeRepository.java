package co.com.pragma.model.mensaje.gateways;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface MensajeRepository {
    Mono<Solicitud> enviarSolicitudActualizada(Solicitud solicitud);
}
