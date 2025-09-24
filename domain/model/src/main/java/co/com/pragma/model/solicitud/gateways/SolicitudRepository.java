package co.com.pragma.model.solicitud.gateways;

import co.com.pragma.model.solicitud.Solicitud;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface SolicitudRepository {
    Mono<Solicitud> rollback(Solicitud solicitud);
    Mono<Solicitud> guardar(Solicitud solicitud);
    Mono<Solicitud> obtenerSolicitudPorId(Long solicitudId);
    Flux<Solicitud> obtenerSolicitudesPorEstado(Integer estadoId, Integer limit, Integer offset);
    Flux<Solicitud> obtenerSolicitudesAprobadaPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    Flux<Solicitud> obtenerSolicitudesPorDocumentoIdAprobadas(Long documentoId);
    Mono<Long> contarSolicitudesPorEstado(Integer estadoId);
}
