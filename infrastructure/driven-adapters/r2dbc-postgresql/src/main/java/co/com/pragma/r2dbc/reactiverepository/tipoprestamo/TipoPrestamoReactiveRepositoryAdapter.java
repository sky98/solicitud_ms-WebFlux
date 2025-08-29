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
        Long,
        TipoPrestamoReactiveRepository
> implements TipoPrestamoRepository {

    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, TipoPrestamo.class));
    }

    @Override
    public Mono<TipoPrestamo> obtenerPorId(Long tipoPrestamoId) {
        return super.findById(tipoPrestamoId)
                .doOnNext(resp -> log.info("Se valido si existe tipo prestamo con id : {}", tipoPrestamoId))
                .doOnError(e -> {
                    log.error("Error al consultar tipo_prestamo por id : {}", e.getMessage());
                    throw new ErrorPersistencia("Error consultando tipo_prestamo por id", Set.of("tipo_prestamo:id"));
                });
    }

    @Override
    public Mono<Boolean> existeMontoEnRango(Long tipoPrestamoId, BigDecimal monto) {
        return repository.contarMontoEnRangoPorId(tipoPrestamoId, monto)
                .map(conteo -> conteo > 0)
                .doOnNext(tipoPrestamo -> log.info("Consultando si el monto esta en el rango permitido, monto : {}", monto))
                .doOnError(e -> {
                    log.error("Error al validar monto en la tabla tipo_prestamo, error : {}", e.getMessage());
                    throw new ErrorPersistencia("Error validando si el monto de la solicitud esta en un rango permitido", Set.of("tipo_persona:monto"));
                });
    }
}
