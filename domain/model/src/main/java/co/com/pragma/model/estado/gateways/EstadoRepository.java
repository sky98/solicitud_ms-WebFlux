package co.com.pragma.model.estado.gateways;

import reactor.core.publisher.Mono;

public interface EstadoRepository {
    Mono<String> obtenerNombreEstadoPorId(Long id);
}
