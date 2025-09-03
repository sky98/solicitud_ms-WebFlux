package co.com.pragma.model.solicitud.gateways;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudRepository {
    Mono<Solicitud> guardar(Solicitud solicitud);
    Flux<Solicitud> obtenerSolicitudesPorEstado(Integer estadoId, Integer limit, Integer offset);
}
