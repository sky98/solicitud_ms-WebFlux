package co.com.pragma.r2dbc.reactiverepository.estadosolicitud;

import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.r2dbc.entity.EstadoSolicitudEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Repository
public class EstadoSolicitudReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Estado,
        EstadoSolicitudEntity,
        Long,
        EstadoSolicitudReactiveRepository
> implements EstadoRepository {

    public EstadoSolicitudReactiveRepositoryAdapter(EstadoSolicitudReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, EstadoSolicitudEntity -> mapper.map(EstadoSolicitudEntity, Estado.class));
    }


    @Override
    public Mono<String> obtenerNombreEstadoPorId(Long id) {
        return repository.findNombreById(id)
                .doOnNext(resp -> log.info("Consulta del nombre del estado por id : {}, correcta ",id))
                .onErrorResume(e -> {
                    log.error("Se presento un error al consultar el nombre del estado con id : {}", id);
                    return Mono.error(new ErrorPersistencia("Error al consultar nombre de estado por id", Set.of(e.getMessage())));
                });
    }

    @Override
    public Mono<Boolean> existeEstadoPorId(Long id) {
        return repository.existsByEstadoId(id)
                .doOnNext(existe -> log.info("Estado con id : {}, existe : {}", id, existe))
                .onErrorResume(e -> {
                    log.error("Se presento un error al validar si existe estado con id : {}", id);
                    return Mono.error(new ErrorPersistencia("Error al validar estado por id", Set.of(e.getMessage())));
                });
    }
}
