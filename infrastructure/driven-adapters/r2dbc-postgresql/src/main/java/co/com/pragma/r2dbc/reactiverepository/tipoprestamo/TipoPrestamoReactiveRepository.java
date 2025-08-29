package co.com.pragma.r2dbc.reactiverepository.tipoprestamo;

import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TipoPrestamoReactiveRepository extends ReactiveCrudRepository<TipoPrestamoEntity, Long>, ReactiveQueryByExampleExecutor<TipoPrestamoEntity> {
    @Query("SELECT COUNT(*) FROM tipo_prestamos tp WHERE tp.tipo_prestamo_id = :tipoPrestamoId AND :monto BETWEEN tp.monto_minimo AND tp.monto_maximo")
    Mono<Long> contarMontoEnRangoPorId(Long tipoPrestamoId, BigDecimal monto);
}
