package co.com.pragma.consumer.circuitbreaker;

import co.com.pragma.model.usuario.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CircuitBreakerFallbacks {

    public Mono<Boolean> validarUsuarioFallback(Long documentoId, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de validar usuario por documentoId: {}. Causa: {}", documentoId, throwable.getMessage());
        return Mono.just(false);
    }

    public Mono<Usuario> obtenerUsuarioFallback(Long documentoId, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de obtener usuario por documentoId: {}. Causa: {}", documentoId, throwable.getMessage());
        return Mono.empty();
    }

}
