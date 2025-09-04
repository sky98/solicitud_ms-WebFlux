package co.com.pragma.consumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RestConsumerTest {

    private static RestConsumerUsuario restConsumer;

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumerUsuario(webClient);
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

}