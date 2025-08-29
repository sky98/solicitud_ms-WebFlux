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

    private final String MENSAJE_ERROR_OBTENER_ID = "Error al consultar tipo_prestamo por id, error : ";
    private final String MENSAJE_ERROR_VALIDAR_MONTO = "Error al validar monto en la tabla tipo_prestamo, error : ";

    public TipoPrestamoReactiveRepositoryAdapter(TipoPrestamoReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, TipoPrestamo.class));
    }

    @Override
    public Mono<TipoPrestamo> obtenerPorId(Long tipoPrestamoId) {
        return super.findById(tipoPrestamoId)
                .doOnNext(resp -> log.info("Se valido si existe tipo prestamo con id : {}", tipoPrestamoId))
                .onErrorResume(e -> {
                    log.error(MENSAJE_ERROR_OBTENER_ID + "{}", e.getMessage());
                    return Mono.error(new ErrorPersistencia(MENSAJE_ERROR_OBTENER_ID + e.getMessage(), Set.of("tipo_prestamo:tipo_prestamo_id")));
                });
    }

    @Override
    public Mono<Boolean> existeMontoEnRango(Long tipoPrestamoId, BigDecimal monto) {
        return repository.contarMontoEnRangoPorId(tipoPrestamoId, monto)
                .map(conteo -> conteo > 0)
                .doOnNext(tipoPrestamo -> log.info("Se valido si el monto esta en el rango permitido, monto : {}", monto))
                .onErrorResume(e -> {
                    log.error(MENSAJE_ERROR_VALIDAR_MONTO + "{}", e.getMessage());
                    return Mono.error(new ErrorPersistencia(MENSAJE_ERROR_VALIDAR_MONTO + e.getMessage(), Set.of("tipo_persona:monto")));
                });
    }
}
