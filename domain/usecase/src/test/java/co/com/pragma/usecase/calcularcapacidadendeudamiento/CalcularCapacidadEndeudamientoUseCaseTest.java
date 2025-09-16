package co.com.pragma.usecase.calcularcapacidadendeudamiento;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalcularCapacidadEndeudamientoUseCaseTest {

    @Mock
    private SolicitudRepository mockSolicitudRepository;

    @Mock
    private MensajeSQSGateway mockMensajeSQSGateway;

    @InjectMocks
    private CalcularCapacidadEndeudamientoUseCase useCase;

    private final Solicitud solicitudValida = SolicitudFabrica.builder().build();
    private final Solicitud solicitudInvalida = SolicitudFabrica.builder()
            .with()
            .solicitudId(2L)
            .estadoId(2L)
            .build();

    @Test
    void testEjecutar_SolicitudNoEncontrada_DebeLanzarError() {
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(99L);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorValidacion &&
                                throwable.getMessage().equals("Solicitud no existe en el sistema : 99")
                )
                .verify();
    }

    @Test
    void testEjecutar_EstadoInvalido_DebeLanzarError() {
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudInvalida));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudInvalida.getSolicitudId());

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorValidacion &&
                                throwable.getMessage().equals("La solicitud debe estar pendiente de revision para poder ejecutar este comando")
                )
                .verify();

        verify(mockMensajeSQSGateway, never()).calcularCapacidadEndeudamiento(any(Solicitud.class));
    }

    @Test
    void testEjecutar_TodoCorrecto_DebeLlamarAlGateway() {
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudValida));
        when(mockMensajeSQSGateway.calcularCapacidadEndeudamiento(any(Solicitud.class))).thenReturn(Mono.just(solicitudValida));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudValida.getSolicitudId());

        StepVerifier.create(resultado)
                .expectNext(solicitudValida)
                .verifyComplete();

        verify(mockMensajeSQSGateway).calcularCapacidadEndeudamiento(solicitudValida);
    }

}
