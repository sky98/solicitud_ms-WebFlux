package co.com.pragma.usecase.actualizarestadosolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.fabricas.EstadoFabrica;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.mensaje.gateways.EncolarMensajeGateway;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActualizarEstadoSolicitudUseCaseTest {

    @Mock
    private SolicitudRepository mockSolicitudRepository;
    @Mock
    private EstadoRepository mockEstadoRepository;
    @Mock
    private EncolarMensajeGateway mockEncolarMensajeGateway;
    @InjectMocks
    private ActualizarEstadoSolicitudUseCase useCase;

    private final Solicitud solicitudExistente = SolicitudFabrica.builder().build();

    private final Solicitud solicitudAActualizar = SolicitudFabrica.builder()
            .with()
            .estadoId(2L)
            .build();

    private final Solicitud solicitudAActualizarRechazada = SolicitudFabrica.builder()
            .with()
            .solicitudId(solicitudExistente.getSolicitudId())
            .estadoId(3L)
            .build();

    private final Estado estadoAprobado = EstadoFabrica.builder()
            .with()
            .estadoId(1L)
            .nombre("Aprobada")
            .build();

    private final Estado estadoRechazado = EstadoFabrica.builder()
            .with()
            .estadoId(3L)
            .nombre("Rechazada")
            .build();

    private final Estado estadoInvalido = EstadoFabrica.builder()
            .with()
            .estadoId(4L)
            .nombre("En proceso")
            .build();

    @Test
    void testActualizarEstado_TodoCorrecto_DebeActualizar(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudExistente));
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estadoAprobado));
        when(mockSolicitudRepository.guardar(any(Solicitud.class))).thenReturn(Mono.just(solicitudAActualizar));
        when(mockEncolarMensajeGateway.enviarSolicitudActualizada(any(Solicitud.class))).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizar);

        StepVerifier.create(resultado)
                .expectNext(solicitudAActualizar)
                .verifyComplete();

        verify(mockSolicitudRepository).obtenerSolicitudPorId(solicitudAActualizar.getSolicitudId());
        verify(mockEstadoRepository).obtenerPorId(solicitudAActualizar.getEstadoId());
        verify(mockSolicitudRepository).guardar(any(Solicitud.class));
        verify(mockEncolarMensajeGateway).enviarSolicitudActualizada(solicitudAActualizar);
        verify(mockSolicitudRepository, never()).rollback(any(Solicitud.class));
    }

    @Test
    void testActualizarEstado_EstadoRechazado_DebeActualizar(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudExistente));
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estadoRechazado));
        when(mockSolicitudRepository.guardar(any(Solicitud.class))).thenReturn(Mono.just(solicitudAActualizarRechazada));
        when(mockEncolarMensajeGateway.enviarSolicitudActualizada(any(Solicitud.class))).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizarRechazada);

        StepVerifier.create(resultado)
                .expectNext(solicitudAActualizarRechazada)
                .verifyComplete();

        verify(mockSolicitudRepository).obtenerSolicitudPorId(solicitudAActualizarRechazada.getSolicitudId());
        verify(mockEstadoRepository).obtenerPorId(solicitudAActualizarRechazada.getEstadoId());
        verify(mockSolicitudRepository).guardar(any(Solicitud.class));
        verify(mockEncolarMensajeGateway).enviarSolicitudActualizada(any(Solicitud.class));
        verify(mockSolicitudRepository, never()).rollback(any(Solicitud.class));
    }

    @Test
    void testActualizarEstado_SolicitudNoEncontrada_DebeLanzarErrorDominio(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizar);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("Solicitud con id : " + solicitudAActualizar.getSolicitudId() + ", no encontrada") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("solicitudId:"+solicitudAActualizar.getSolicitudId()))
                )
                .verify();

        verify(mockEstadoRepository, never()).obtenerPorId(anyLong());
        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testActualizarEstado_EstadoNoEncontrado_DebeLanzarErrorDominio(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudExistente));
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizar);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("Estado a actualizar no es el correcto") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of(solicitudAActualizar.getEstadoId().toString()))
                )
                .verify();

        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testActualizarEstado_EstadoNoValido_DebeLanzarErrorDominio(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudExistente));
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estadoInvalido));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizar);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("Estado a actualizar no es valido (Aprobada, Rechazada)") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("estadoId:"+solicitudAActualizar.getEstadoId()))
                )
                .verify();

        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testActualizarEstado_FallaAlEnviarMensaje_DebeHacerRollback(){
        when(mockSolicitudRepository.obtenerSolicitudPorId(anyLong())).thenReturn(Mono.just(solicitudExistente));
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estadoAprobado));
        when(mockSolicitudRepository.guardar(any(Solicitud.class))).thenReturn(Mono.just(solicitudAActualizar));
        when(mockEncolarMensajeGateway.enviarSolicitudActualizada(any(Solicitud.class))).thenReturn(Mono.error(new RuntimeException("Fallo en la cola de mensajes")));
        when(mockSolicitudRepository.rollback(any(Solicitud.class))).thenReturn(Mono.just(solicitudExistente));
        when(mockEncolarMensajeGateway.enviarSolicitudAprobada(any(Solicitud.class))).thenReturn(Mono.just(solicitudExistente));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudAActualizar);

        StepVerifier.create(resultado)
                .expectNext(solicitudAActualizar)
                .verifyComplete();

        verify(mockSolicitudRepository).obtenerSolicitudPorId(solicitudAActualizar.getSolicitudId());
        verify(mockEstadoRepository).obtenerPorId(solicitudAActualizar.getEstadoId());
        verify(mockSolicitudRepository).guardar(any(Solicitud.class));
        verify(mockEncolarMensajeGateway).enviarSolicitudActualizada(solicitudAActualizar);
        verify(mockSolicitudRepository).rollback(solicitudExistente);
    }

}
