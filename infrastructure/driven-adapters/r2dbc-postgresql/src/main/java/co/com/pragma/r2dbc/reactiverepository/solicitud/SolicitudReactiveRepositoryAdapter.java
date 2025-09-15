package co.com.pragma.r2dbc.reactiverepository.solicitud;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
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
    public Mono<Solicitud> rollback(Solicitud solicitud) {
        log.info("Realizando Rollback de la operacion anterior.");
        return guardar(solicitud);
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

    @Override
    public Mono<Solicitud> obtenerSolicitudPorId(Long solicitudId) {
        return repository.findBySolicitudId(solicitudId)
                .onErrorResume(e -> {
                    log.error("Se genero un error al consultar solicitud con id : {}", solicitudId);
                    return Mono.error(new ErrorPersistencia("Error al consultar solicitud por id : "+ solicitudId, Set.of(e.getMessage())));
                });
    }

    @Override
    public Flux<Solicitud> obtenerSolicitudesPorEstado(Integer estadoId, Integer limit, Integer offset) {
        log.info("Consultando solicitudes con estadoId : {}", estadoId);
        return repository.findByEstadoIdWithPagination(Long.valueOf(estadoId), limit, offset)
                .onErrorResume(e -> {
                    log.error("Se genero un error al consultar solicitudes con estadoId : {}", estadoId);
                    return Mono.error(new ErrorPersistencia("Error al consultar solicitudes por estado", Set.of(e.getMessage())));
                });
    }

    @Override
    public Mono<Long> contarSolicitudesPorEstado(Integer estadoId) {
        return repository.contarSolicitudesPorEstado(estadoId)
                .doOnNext(solicitudes -> log.info("Se consulto con exito el numero total de las solicitudes con estadoId : {}", estadoId))
                .onErrorResume(e -> {
                    log.error("Se genero un error al consultar el numero total de las solicitudes con estadoId : {}", estadoId);
                    return Mono.error(new ErrorPersistencia("Error al consultar total de solicitudes por estado", Set.of(e.getMessage())));
                });
    }
}
