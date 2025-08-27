package co.com.pragma.r2dbc.reactiverepository.tipoprestamo;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import co.com.pragma.r2dbc.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Repository
public class TipoPrestamoReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        TipoPrestamo,
        TipoPrestamoEntity,
        String,
        TipoPrestamoReactiveRepository
> implements TipoPrestamoRepository {
    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, TipoPrestamoEntity -> mapper.map(TipoPrestamoEntity, TipoPrestamo.class));
    }

    @Override
    public Mono<TipoPrestamo> existePorNombre(String nombre) {
        return repository.exitsByNombre(nombre)
                .doOnNext(tipoPrestamo -> log.info("Consultando si existe tipo_prestamo con nombre : {}", nombre))
                .doOnError(e -> {
                    log.error("Error al consultar tipo_prestamo por nombre, error : {}", e.getMessage());
                    throw new ErrorPersistencia("Error al consultar en la tabla tipo_persona", Set.of("tipo_persona:nombre"));
                });
    }

    @Override
    public Mono<Boolean> existeMontoEnRango(Long tipoPrestamoId, BigDecimal monto) {
        return repository.existeMontoEnRangoPorId(tipoPrestamoId, monto)
                .doOnNext(tipoPrestamo -> log.info("Consultando si existe el monto esta en el rango permitido, monto : {}", monto))
                .doOnError(e -> {
                    log.error("Error al validar monto en la tabla tipo_prestamo, error : {}", e.getMessage());
                    throw new ErrorPersistencia("Error al consultar en la tabla tipo_persona", Set.of("tipo_persona:monto"));
                });
    }
}
