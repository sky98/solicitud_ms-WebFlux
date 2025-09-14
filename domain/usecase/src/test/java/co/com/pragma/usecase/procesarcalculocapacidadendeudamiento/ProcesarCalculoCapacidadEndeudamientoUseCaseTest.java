package co.com.pragma.usecase.procesarcalculocapacidadendeudamiento;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.fabricas.MensajeProcesadoSolicitudFabrica;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.model.mensaje.gateways.MensajeUtilsGateway;
import co.com.pragma.model.solicitud.EstadoSolicitud;
import co.com.pragma.model.solicitud.MensajeProcesadoSolicitud;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcesarCalculoCapacidadEndeudamientoUseCaseTest {

    @Mock
    private MensajeUtilsGateway mockMensajeUtilsGateway;

    @Mock
    private SolicitudRepository mockSolicitudRepository;

    @InjectMocks
    private ProcesarCalculoCapacidadEndeudamientoUseCase useCase;

    private Solicitud solicitudMock = SolicitudFabrica.builder()
            .with()
            .solicitudId(526L)
            .build();
    private MensajeProcesadoSolicitud mensajeMock = MensajeProcesadoSolicitudFabrica.builder()
            .with()
            .solicitudId(526)
            .estado(EstadoSolicitud.PENDIENTE_REVISION.name())
            .build();
    private String mensajeBodyValido = "{\"solicitudId\": 526, \"estado\": \"APROBADO\"}";
    private String mensajeBodyInvalido = "{\"solicitudId\": 526, \"estado\": \"ESTADO_INEXISTENTE\"}";


    @Test
    void testEjecutar_HappyPath_DebeGuardarSolicitud() {
        when(mockMensajeUtilsGateway.deserializarMensaje(eq(mensajeBodyValido), eq(MensajeProcesadoSolicitud.class)))
                .thenReturn(Mono.just(mensajeMock));
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong()))
                .thenReturn(Mono.just(solicitudMock));
        when(mockSolicitudRepository.guardar(any(Solicitud.class)))
                .thenReturn(Mono.just(solicitudMock));

        Mono<Solicitud> resultado = useCase.ejecutar(mensajeBodyValido);

        StepVerifier.create(resultado)
                .expectNext(solicitudMock)
                .verifyComplete();

        verify(mockMensajeUtilsGateway).deserializarMensaje(eq(mensajeBodyValido), eq(MensajeProcesadoSolicitud.class));
        verify(mockSolicitudRepository).obtenerSolicitudPorId(solicitudMock.getSolicitudId());
        verify(mockSolicitudRepository).guardar(solicitudMock);
    }

    @Test
    void testEjecutar_MensajeInvalido_DebeLanzarError() {
        when(mockMensajeUtilsGateway.deserializarMensaje(any(String.class), eq(MensajeProcesadoSolicitud.class)))
                .thenReturn(Mono.error(new ErrorValidacion("Error al parsear JSON", Set.of("JSON_INVALIDO"))));

        Mono<Solicitud> resultado = useCase.ejecutar("JSON inválido");

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorValidacion &&
                                throwable.getMessage().contains("Error al parsear JSON")
                )
                .verify();
    }

}
