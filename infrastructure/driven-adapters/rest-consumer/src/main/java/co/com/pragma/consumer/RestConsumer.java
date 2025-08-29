package co.com.pragma.consumer;

import co.com.pragma.model.solicitud.gateways.ResConsumerGateway;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements ResConsumerGateway {

    private final WebClient client;
    private final String PATH_VALIDAR_USUARIO_POR_DOCUMENTO_ID = "/api/v1/usuarios/documento/{documentoId}/existe";


    // these methods are an example that illustrates the implementation of WebClient.
    // You should use the methods that you implement from the Gateway from the domain.
    @CircuitBreaker(name = "testGet" /*, fallbackMethod = "testGetOk"*/)
    public Mono<ObjectResponse> validarDocumentoIdUsuario(Long documentoId) {
        return client
                .get()
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }

// Possible fallback method
//    public Mono<String> testGetOk(Exception ignored) {
//        return client
//                .get() // TODO: change for another endpoint or destination
//                .retrieve()
//                .bodyToMono(String.class);
//    }

    @CircuitBreaker(name = "testPost")
    public Mono<ObjectResponse> testPost() {
        ObjectRequest request = ObjectRequest.builder()
            .val1("exampleval1")
            .val2("exampleval2")
            .build();
        return client
                .post()
                .body(Mono.just(request), ObjectRequest.class)
                .retrieve()
                .bodyToMono(ObjectResponse.class);
    }

    @Override
    public Mono<Boolean> validarUsuarioPorDocumentoId(Long documentoId) {
        log.info("Realizando peticion externa para validar si existe usuario con documentoId : {}", documentoId);
        return client
                .get()
                .uri(PATH_VALIDAR_USUARIO_POR_DOCUMENTO_ID, documentoId)
                .retrieve()
                .bodyToMono(ObjectResponse.class)
                .doOnNext(resp -> log.info("Se obtiene respuesta externa de validacion del usuario, existe : {}", resp.isExiste()))
                .map(ObjectResponse::isExiste);
    }
}
