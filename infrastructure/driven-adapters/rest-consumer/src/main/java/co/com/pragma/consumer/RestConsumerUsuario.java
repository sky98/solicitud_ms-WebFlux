package co.com.pragma.consumer;

import co.com.pragma.consumer.response.ValidarUsuarioPorIdResponse;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.model.usuario.Usuario;
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
public class RestConsumerUsuario implements UsuarioResConsumerGateway {

    private final WebClient client;
    private final String PATH_VALIDAR_USUARIO_POR_DOCUMENTO_ID = "/api/v1/usuarios/documento/{documentoId}/existe";
    private final String PATH_OBTENER_USUARIO_POR_DOCUMENTO_ID = "/api/v1/usuarios/documento/{documentoId}";

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

    @Override
    @CircuitBreaker(name = "obtenerUsuario", fallbackMethod = "obtenerUsuarioFallback")
    public Mono<Usuario> obtenerUsuarioPorDocumentoId(Long documentoId) {
        log.info("Realizando solicitud externa para obtener datos del usuario con documentoId : {}", documentoId);
        return client
                .get()
                .uri(PATH_OBTENER_USUARIO_POR_DOCUMENTO_ID, documentoId)
                .retrieve()
                .bodyToMono(Usuario.class);
    }

    private Mono<Boolean> validarUsuarioFallback(Long documentoId, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de validar usuario por documentoId: {}. Causa: {}", documentoId, throwable.getMessage());
        return Mono.just(false);
    }

    private Mono<Usuario> obtenerUsuarioFallback(Long documentoId, Throwable throwable) {
        log.error("Se activa el fallback para la peticion de obtener usuario por documentoId: {}. Causa: {}", documentoId, throwable.getMessage());
        return Mono.empty();
    }
}
