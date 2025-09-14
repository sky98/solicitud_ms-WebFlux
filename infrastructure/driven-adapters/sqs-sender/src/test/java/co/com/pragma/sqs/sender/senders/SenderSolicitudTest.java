package co.com.pragma.sqs.sender.senders;

import co.com.pragma.errores.ErrorSQS;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.sqs.sender.mapper.MapperMensajesUtils;
import co.com.pragma.sqs.sender.mapper.SolicitudMensajeMapper;
import co.com.pragma.sqs.sender.mensajes.ActualizarEstadoSolicitudMensaje;
import co.com.pragma.sqs.sender.mensajes.CalcularCapacidadEndeudamientoMensaje;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SenderSolicitudTest {

    @Mock
    private EstadoRepository mockEstadoRepository;
    @Mock
    private TipoPrestamoRepository mockTipoPrestamoRepository;
    @Mock
    private SolicitudRepository mockSolicitudRepository;
    @Mock
    private UsuarioResConsumerGateway mockUsuarioResConsumerGateway;
    @Mock
    private SolicitudMensajeMapper mockSolicitudMensajeMapper;
    @Mock
    private SQSSender mockSqsSender;
    @Mock
    private MapperMensajesUtils mockMapperMensajesUtils;

    @InjectMocks
    private SenderSolicitud sender;

    private Solicitud solicitud = Solicitud.builder()
            .solicitudId(1L)
            .tipoPrestamoId(1L)
            .documentoId(123L)
            .monto(BigDecimal.valueOf(1000000))
            .plazo(12L)
            .build();
    private ActualizarEstadoSolicitudMensaje actualizarEstadoSolicitudMensaje = ActualizarEstadoSolicitudMensaje.builder()
            .solicitudId(1L)
                .estado("1")
                .tipoPrestamo("1")
                .build();
    private TipoPrestamo tipoPrestamo = TipoPrestamo.builder()
            .tipoPrestamoId(1L)
            .nombre("Vivienda")
            .tasaInteres(BigDecimal.valueOf(0.05))
            .build();
    private Estado estado = Estado.builder()
            .estadoId(1L)
            .nombre("APROBADA")
            .build();
    private Usuario usuario = Usuario.builder()
            .nombres("Juan")
            .apellidos("Perez")
            .salarioBase(BigDecimal.valueOf(2000000))
            .build();

    @Test
    @DisplayName("Test exitoso: Enviar solicitud actualizada a SQS")
    void testEnviarSolicitudActualizada_HappyPath_ShouldReturnMappedSolicitud() {
        when(mockSolicitudMensajeMapper.toMessage(any(Solicitud.class))).thenReturn(actualizarEstadoSolicitudMensaje);
        when(mockEstadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estado));
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamo));
        when(mockMapperMensajesUtils.serializar(any(ActualizarEstadoSolicitudMensaje.class))).thenReturn(Mono.just("json-message"));
        when(mockSqsSender.send(any(String.class), any(String.class))).thenReturn(Mono.just("message-id-123"));

        Mono<Solicitud> result = sender.enviarSolicitudActualizada(solicitud);

        StepVerifier.create(result)
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test exitoso: Calcular capacidad de endeudamiento y enviar a SQS")
    void testCalcularCapacidadEndeudamiento_HappyPath_ShouldReturnSolicitud() {
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamo));
        when(mockSolicitudRepository.obtenerSolicitudesPorDocumentoIdAprobadas(anyLong()))
                .thenReturn(Flux.just(solicitud));
        when(mockUsuarioResConsumerGateway.obtenerUsuarioPorDocumentoId(anyLong()))
                .thenReturn(Mono.just(usuario));
        when(mockMapperMensajesUtils.serializar(any(CalcularCapacidadEndeudamientoMensaje.class))).thenReturn(Mono.just("json-message"));
        when(mockSqsSender.send(any(String.class), any(String.class))).thenReturn(Mono.just("message-id-456"));

        Mono<Solicitud> result = sender.calcularCapacidadEndeudamiento(solicitud);

        StepVerifier.create(result)
                .expectNext(solicitud)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test de error: Falla el envío de mensaje a SQS")
    void testCalcularCapacidadEndeudamiento_SenderError_ShouldReturnErrorSQS() {
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamo));
        when(mockSolicitudRepository.obtenerSolicitudesPorDocumentoIdAprobadas(anyLong()))
                .thenReturn(Flux.just(solicitud));
        when(mockUsuarioResConsumerGateway.obtenerUsuarioPorDocumentoId(anyLong()))
                .thenReturn(Mono.just(usuario));
        when(mockMapperMensajesUtils.serializar(any(CalcularCapacidadEndeudamientoMensaje.class))).thenReturn(Mono.just("json-message"));
        when(mockSqsSender.send(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Error en SQS")));

        Mono<Solicitud> result = sender.calcularCapacidadEndeudamiento(solicitud);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ErrorSQS)
                .verify();
    }
}
