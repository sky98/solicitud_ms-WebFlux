package co.com.pragma.model.mensaje.gateways;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;

public interface MensajeSQSGateway {
    Mono<Solicitud> enviarSolicitudActualizada(Solicitud solicitud);
    Mono<Solicitud> calcularCapacidadEndeudamiento(Solicitud solicitud);
}
