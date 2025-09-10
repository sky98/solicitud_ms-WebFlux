package co.com.pragma.r2dbc.reactiverepository.solicitud;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SolicitudReactiveRepository extends ReactiveCrudRepository<SolicitudEntity, String>, ReactiveQueryByExampleExecutor<SolicitudEntity> {

    @Query("SELECT * FROM solicitudes WHERE estado_id = :estadoId ORDER BY solicitud_id LIMIT :limit OFFSET :offset")
    Flux<Solicitud> findByEstadoIdWithPagination(Long estadoId, Integer limit, Integer offset);

    @Query("SELECT COUNT(*) FROM solicitudes WHERE estado_id = :estadoId")
    Mono<Long> contarSolicitudesPorEstado(Integer estadoId);

    Mono<Solicitud> findBySolicitudId(Long solicitudId);

}
