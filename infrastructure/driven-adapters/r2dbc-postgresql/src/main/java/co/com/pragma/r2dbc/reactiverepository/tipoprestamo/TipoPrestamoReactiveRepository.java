package co.com.pragma.r2dbc.reactiverepository.tipoprestamo;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TipoPrestamoReactiveRepository extends ReactiveCrudRepository<TipoPrestamoEntity, String>, ReactiveQueryByExampleExecutor<TipoPrestamoEntity> {
    Mono<TipoPrestamo> exitsByNombre(String nombre);
    @Query("SELECT COUNT(*) > 0 FROM tipo_prestamo tp WHERE tp.tipo_prestamo_id = :tipoPrestamoId AND :monto BETWEEN tp.monto_minimo AND tp.monto_maximo")
    Mono<Boolean> existeMontoEnRangoPorId(Long tipoPrestamoId, BigDecimal monto);
}
