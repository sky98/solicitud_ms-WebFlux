package co.com.pragma.api;

import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.api.handlers.Handler;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.api.router.GuardarSolicitudRouter;
import co.com.pragma.api.validador.ValidadorRequest;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.usecase.guardarsolicitud.GuardarSolicitudUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class, GuardarSolicitudRouter.class})
@WebFluxTest
class RouterRestTest {

    private final String BASE_PATH = "/api/v1/solicitudes";

    @MockitoBean
    private GuardarSolicitudUseCase guardarSolicitudUseCase;
    @MockitoBean
    private ValidadorRequest validador;
    @MockitoBean
    private SolicitudMapper mapper;

    @Autowired
    private WebTestClient webTestClient;

    private Solicitud solicitudFabrica = Solicitud.builder()
            .solicitudId(1L)
            .monto(BigDecimal.valueOf(50000))
            .plazo(12L)
            .estadoId(1L)
            .tipoPrestamoId(1L)
            .documentoId(1233456L)
            .build();

    private CrearSolicitudRequestDTO crearSolicitudRequestDTOFabrica = CrearSolicitudRequestDTO.builder()
            .tipoPrestamoId(BigDecimal.ONE)
            .monto(BigDecimal.valueOf(50000))
            .plazo(BigDecimal.TEN)
            .tipoPrestamoId(BigDecimal.ONE)
            .documentoId("1233456L")
            .build();

    private SolicitudResponse solicitudResponseFabrica = SolicitudResponse.builder()
            .solicitudId(1L)
            .tipoPrestamoId(1L)
            .monto(BigDecimal.valueOf(50000))
            .plazo(1L)
            .documentoId(1233456L)
            .estadoId(1L)
            .build();

    @Test
    void testGuardarSolicitudUseCase() {

        when(mapper.toModel(any(CrearSolicitudRequestDTO.class))).thenReturn(solicitudFabrica);
        when(mapper.toResponse(any(Solicitud.class))).thenReturn(solicitudResponseFabrica);
        when(validador.validar(any(CrearSolicitudRequestDTO.class))).thenReturn(Mono.just(crearSolicitudRequestDTOFabrica));
        when(guardarSolicitudUseCase.ejecutar(any(Solicitud.class), any(String.class))).thenReturn(Mono.just(solicitudFabrica));

        webTestClient.post()
                .uri(BASE_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(crearSolicitudRequestDTOFabrica)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SolicitudResponse.class)
                .value(solicitudResponse -> {
                    Assertions.assertThat(solicitudResponse.solicitudId()).isEqualTo(solicitudFabrica.getSolicitudId());
                        }
                );
    }

}
