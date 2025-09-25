package co.com.pragma.usecase.guardarsolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.fabricas.TipoPrestamoFabrica;
import co.com.pragma.model.mensaje.gateways.EncolarMensajeGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GuardarSolicitudUseCaseTest {

    @Mock
    private SolicitudRepository mockSolicitudRepository;
    @Mock
    private TipoPrestamoRepository mockTipoPrestamoRepository;
    @Mock
    private UsuarioResConsumerGateway mockUsuarioResConsumerGateway;
    @Mock
    private EncolarMensajeGateway mockEncolarMensajeGateway;

    @InjectMocks
    private GuardarSolicitudUseCase useCase;

    private Solicitud solicitudBuilder = SolicitudFabrica.builder().build();
    private TipoPrestamo tipoPrestamoBuilder = TipoPrestamoFabrica.builder().build();
    private final String usuarioAutenticado = "1233456";

    @Test
    void testGuardarSolicitud_TodoCorrecto_DebeGuardar(){
        when(mockUsuarioResConsumerGateway.validarUsuarioPorDocumentoId(anyLong())).thenReturn(Mono.just(true));
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamoBuilder));
        when(mockTipoPrestamoRepository.existeMontoEnRango(anyLong(), any(BigDecimal.class))).thenReturn(Mono.just(true));
        when(mockSolicitudRepository.guardar(any(Solicitud.class))).thenReturn(Mono.just(solicitudBuilder));
        when(mockEncolarMensajeGateway.calcularCapacidadEndeudamiento(any(Solicitud.class))).thenReturn(Mono.just(solicitudBuilder));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudBuilder, usuarioAutenticado);

        StepVerifier.create(resultado)
                .expectNextMatches(solicitud -> solicitud.getSolicitudId() != null)
                .verifyComplete();

        verify(mockUsuarioResConsumerGateway).validarUsuarioPorDocumentoId(solicitudBuilder.getDocumentoId());
        verify(mockTipoPrestamoRepository).obtenerPorId(solicitudBuilder.getTipoPrestamoId());
        verify(mockTipoPrestamoRepository).existeMontoEnRango(solicitudBuilder.getTipoPrestamoId(), solicitudBuilder.getMonto());
        verify(mockSolicitudRepository).guardar(solicitudBuilder);
    }

    @Test
    void testGuardarSolicitud_UsuarioNoExiste_DebeLanzarErrorDominio(){
        when(mockUsuarioResConsumerGateway.validarUsuarioPorDocumentoId(anyLong())).thenReturn(Mono.just(false));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudBuilder, usuarioAutenticado);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals(String.format("Usuario con documentoId: %d no existe en el sistema", solicitudBuilder.getDocumentoId())) &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("usuario:documentoId"))
                )
                .verify();

        verify(mockTipoPrestamoRepository, never()).obtenerPorId(anyLong());
        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testGuardarSolicitud_TipoPrestamoNoExiste_DebeLanzarErrorDominio(){
        when(mockUsuarioResConsumerGateway.validarUsuarioPorDocumentoId(anyLong())).thenReturn(Mono.just(true));
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.empty());

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudBuilder, usuarioAutenticado);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("El tipo de prestamo no existe") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("tipoPrestamoId"))
                )
                .verify();

        verify(mockTipoPrestamoRepository).obtenerPorId(solicitudBuilder.getTipoPrestamoId());
        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testGuardarSolicitud_MontoNoCumpleRango_DebeLanzarErrorDominio(){
        when(mockUsuarioResConsumerGateway.validarUsuarioPorDocumentoId(anyLong())).thenReturn(Mono.just(true));
        when(mockTipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamoBuilder));
        when(mockTipoPrestamoRepository.existeMontoEnRango(anyLong(), any(BigDecimal.class))).thenReturn(Mono.just(false));

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudBuilder, usuarioAutenticado);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("Monto no cumple con el rango del tipo de prestamo") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("monto"))
                )
                .verify();

        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

    @Test
    void testGuardarSolicitud_QuienRealizaLaPeticionNoEsQuienSolicita_DebeLanzarErrorDominio(){

        Mono<Solicitud> resultado = useCase.ejecutar(solicitudBuilder, "1");

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorDominio &&
                                throwable.getMessage().equals("El documento de la solicitud no coincide con el documento del usuario autenticado.") &&
                                ((ErrorDominio) throwable).getCampos().equals(Set.of("usuario:documentoId"))
                )
                .verify();

        verify(mockSolicitudRepository, never()).guardar(any(Solicitud.class));
    }

}
