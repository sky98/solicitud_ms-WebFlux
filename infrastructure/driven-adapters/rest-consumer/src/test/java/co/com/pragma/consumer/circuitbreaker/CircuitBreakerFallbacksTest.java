package co.com.pragma.consumer.circuitbreaker;

import co.com.pragma.model.usuario.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CircuitBreakerFallbacksTest {

    private CircuitBreakerFallbacks fallbacks = new CircuitBreakerFallbacks();

    @Test
    @DisplayName("Test de fallback: validarUsuarioFallback debe retornar Mono con false")
    void testValidarUsuarioFallback_ShouldReturnFalse() {
        Long documentoId = 12345L;
        Throwable throwable = new RuntimeException("Simulated exception");

        Mono<Boolean> result = fallbacks.validarUsuarioFallback(documentoId, throwable);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test de fallback: obtenerUsuarioFallback debe retornar Mono vacío")
    void testObtenerUsuarioFallback_ShouldReturnEmpty() {
        Long documentoId = 67890L;
        Throwable throwable = new RuntimeException("Simulated exception");

        Mono<Usuario> result = fallbacks.obtenerUsuarioFallback(documentoId, throwable);

        StepVerifier.create(result)
                .verifyComplete();
    }

}
