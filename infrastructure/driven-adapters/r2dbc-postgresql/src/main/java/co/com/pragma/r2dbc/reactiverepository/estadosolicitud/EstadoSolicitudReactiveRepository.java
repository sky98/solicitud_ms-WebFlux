package co.com.pragma.r2dbc.reactiverepository.estadosolicitud;

import co.com.pragma.model.estado.Estado;
import co.com.pragma.r2dbc.entity.EstadoSolicitudEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EstadoSolicitudReactiveRepository extends ReactiveCrudRepository<EstadoSolicitudEntity, Long>, ReactiveQueryByExampleExecutor<EstadoSolicitudEntity> {
    @Query("SELECT estados.* FROM estados WHERE estado_id = :estadoId")
    Mono<Estado> findByEstadoId(Long estadoId);
}
