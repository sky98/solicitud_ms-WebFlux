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
    public Mono<Estado> obtenerPorId(Long id) {
        return repository.findByEstadoId(id)
                .doOnNext(estado -> log.info("Estado con id {}  obtenido con exito", id))
                .onErrorResume(e -> {
                    log.error("Se presento un error al obtener estado con id : {}", id);
                    return Mono.error(new ErrorPersistencia("Error al obtener estado por id", Set.of(e.getMessage())));
                });
    }
}
