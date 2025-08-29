package co.com.pragma.r2dbc.reactiverepository.solicitud;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import co.com.pragma.r2dbc.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Repository
public class SolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Solicitud,
        SolicitudEntity,
        String,
        SolicitudReactiveRepository
> implements SolicitudRepository {

    private final String MENSAJE_ERROR = "Se ha generado un error al guardar en la tabla solicitud, error : ";
    private final TransactionalOperator transactionalOperator;

    public SolicitudReactiveRepositoryAdapter(SolicitudReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, SolicitudEntity -> mapper.map(SolicitudEntity, Solicitud.class));
        this.transactionalOperator = transactionalOperator;
    }


    @Override
    public Mono<Solicitud> guardar(Solicitud solicitud) {
        return transactionalOperator.execute(
                status -> super.save(solicitud)
        ).singleOrEmpty()
                .onErrorResume(e -> {
                    log.error(MENSAJE_ERROR + "{}", e.getMessage());
                    return Mono.error(new ErrorPersistencia(MENSAJE_ERROR + e.getMessage(), Set.of(e.getMessage())));
                });
    }
}
