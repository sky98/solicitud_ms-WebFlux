package co.com.pragma.consumer;

import co.com.pragma.model.solicitud.gateways.ResConsumerGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements ResConsumerGateway {

    private final WebClient client;
    private final String PATH_VALIDAR_USUARIO_POR_DOCUMENTO_ID = "/api/v1/usuarios/documento/{documentoId}/existe";

    @Override
    @CircuitBreaker(name = "validarUsuario", fallbackMethod = "validarUsuarioFallback")
    public Mono<Boolean> validarUsuarioPorDocumentoId(Long documentoId) {
        log.info("Realizando peticion externa para validar si existe usuario con documentoId : {}", documentoId);
        return client
                .get()
                .uri(PATH_VALIDAR_USUARIO_POR_DOCUMENTO_ID, documentoId)
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, clientResponse -> Mono.just(false).then(Mono.empty()))
                .bodyToMono(ValidarUsuarioPorIdResponse.class)
                .doOnNext(resp -> log.info("Se obtiene respuesta externa de validacion del usuario, existe : {}", resp.isExiste()))
                .map(ValidarUsuarioPorIdResponse::isExiste);
    }

    private Mono<Boolean> validarUsuarioFallback(Long documentoId, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de documentoId: {}. Causa: {}", documentoId, throwable.getMessage());
        return Mono.just(false);
    }
}
