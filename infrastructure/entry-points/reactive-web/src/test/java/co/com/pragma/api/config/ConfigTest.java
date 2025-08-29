package co.com.pragma.api.config;

import co.com.pragma.api.handlers.Handler;
import co.com.pragma.api.RouterRest;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.api.router.GuardarSolicitudRouter;
import co.com.pragma.api.validador.ValidadorRequest;
import co.com.pragma.usecase.guardarsolicitud.GuardarSolicitudUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, GuardarSolicitudRouter.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

    private final String BASE_PATH = "/api/v1/solicitudes";

    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private GuardarSolicitudUseCase guardarSolicitudUseCase;
    @MockitoBean
    private ValidadorRequest validador;
    @MockitoBean
    private SolicitudMapper mapper;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.post()
                .uri(BASE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}