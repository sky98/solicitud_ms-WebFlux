package co.com.pragma.consumer;

import co.com.pragma.consumer.circuitbreaker.CircuitBreakerFallbacks;
import co.com.pragma.model.usuario.Usuario;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;

class RestConsumerTest {

    private static RestConsumerUsuario restConsumer;

    private static MockWebServer mockBackEnd;

    @Mock
    private static CircuitBreakerFallbacks mockFallback;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumerUsuario(webClient, mockFallback);
    }

    @AfterAll
    static void tearDown() throws IOException {

        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Test exitoso: El usuario existe y el servicio responde OK")
    void testValidarUsuarioPorDocumentoId_UsuarioExiste() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"existe\": true}"));

        var documentoId = 123456789L;
        var response = restConsumer.validarUsuarioPorDocumentoId(documentoId);

        StepVerifier.create(response)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test de error: El usuario no existe y el servicio responde OK")
    void testValidarUsuarioPorDocumentoId_UsuarioNoExiste() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"existe\": false}"));

        var documentoId = 987654321L;
        var response = restConsumer.validarUsuarioPorDocumentoId(documentoId);

        StepVerifier.create(response)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test exitoso: Obtener usuario por documentoId")
    void testObtenerUsuarioPorDocumentoId_HappyPath() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"nombres\":\"Juan\",\"apellido\":\"Perez\",\"documentoId\":123456789,\"tipoDocumentoId\":\"CC\"}"));

        var documentoId = 123456789L;
        Mono<Usuario> response = restConsumer.obtenerUsuarioPorDocumentoId(documentoId);

        StepVerifier.create(response)
                .expectNextMatches(usuario -> "Juan".equals(usuario.getNombres()) && 123456789L == usuario.getDocumentoId())
                .verifyComplete();
    }

    @Test
    @DisplayName("Test de error: El servicio de validación de usuario responde 500")
    void testValidarUsuarioPorDocumentoId_ServiceError_DebeLlamarFallback() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        var documentoId = 111L;
        Mono<Boolean> response = restConsumer.validarUsuarioPorDocumentoId(documentoId);

        StepVerifier.create(response)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Test de error: El servicio de obtención de usuario responde 500")
    void testObtenerUsuarioPorDocumentoId_ServiceError_DebeLlamarFallback() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        var documentoId = 222L;
        Mono<Usuario> response = restConsumer.obtenerUsuarioPorDocumentoId(documentoId);

        StepVerifier.create(response)
                .expectError()
                .verify();
    }

}