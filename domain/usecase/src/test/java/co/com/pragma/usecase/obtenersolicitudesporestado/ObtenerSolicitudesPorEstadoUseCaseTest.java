package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.fabricas.TipoPrestamoFabrica;
import co.com.pragma.fabricas.UsuarioFabrica;
import co.com.pragma.model.Paginacion;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObtenerSolicitudesPorEstadoUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    @Mock
    private UsuarioResConsumerGateway usuarioRestConsumerGateway;

    @InjectMocks
    private ObtenerSolicitudesPorEstadoUseCase obtenerSolicitudesPorEstadoUseCase;

    private Solicitud solicitudBuilder = SolicitudFabrica.builder().build();
    private TipoPrestamo tipoPrestamoBuilder = TipoPrestamoFabrica.builder().build();
    private Usuario usuarioBuilder = UsuarioFabrica.builder().build();

    @Test
    void deberiaObtenerSolicitudesPorEstadoConExito() {
        Integer estadoId = 1;
        Integer limit = 10;
        Integer offset = 0;

        when(estadoRepository.existeEstadoPorId(any(Long.class))).thenReturn(Mono.just(true));
        when(solicitudRepository.obtenerSolicitudesPorEstado(any(Integer.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.just(solicitudBuilder));
        when(solicitudRepository.contarSolicitudesPorEstado(any(Integer.class))).thenReturn(Mono.just(1L));
        when(estadoRepository.obtenerNombreEstadoPorId(anyLong())).thenReturn(Mono.just("Aprobada"));
        when(tipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamoBuilder));
        when(usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(anyLong())).thenReturn(Mono.just(usuarioBuilder));

        Mono<Paginacion<DetallesSolicitudes>> resultado = obtenerSolicitudesPorEstadoUseCase.ejecutar(estadoId, limit, offset);

        StepVerifier.create(resultado)
                .assertNext(paginacion -> {
                    assertEquals(1L, paginacion.getTotalElementos());
                    assertEquals(1L, paginacion.getTotalPaginas());
                    assertEquals(1L, paginacion.getNumeroPagina());
                    assertEquals(10L, paginacion.getTamanoPagina());
                    assertEquals(1, paginacion.getItems().size());
                    assertEquals("Aprobada", paginacion.getItems().get(0).getEstadoSolicitud());
                })
                .verifyComplete();
    }

    @Test
    void deberiaLanzarErrorCuandoEstadoNoExiste() {
        Integer estadoId = 99;
        Integer limit = 10;
        Integer offset = 0;

        when(estadoRepository.existeEstadoPorId(any(Long.class))).thenReturn(Mono.just(false));

        Mono<Paginacion<DetallesSolicitudes>> resultado = obtenerSolicitudesPorEstadoUseCase.ejecutar(estadoId, limit, offset);

        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorValidacion &&
                                "Estado no encontrado".equals(throwable.getMessage()) &&
                                ((ErrorValidacion) throwable).getCampos().contains("estadoId")
                )
                .verify();
    }

}
