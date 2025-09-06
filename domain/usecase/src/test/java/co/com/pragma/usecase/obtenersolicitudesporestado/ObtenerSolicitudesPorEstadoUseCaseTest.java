package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.fabricas.EstadoFabrica;
import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.fabricas.TipoPrestamoFabrica;
import co.com.pragma.fabricas.UsuarioFabrica;
import co.com.pragma.model.Paginacion;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
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

    private Estado estado = new EstadoFabrica().build();
    private TipoPrestamo tipoPrestamo = new TipoPrestamoFabrica().build();

    private Usuario usuario1 = new UsuarioFabrica().with().documentoId(123L).nombres("Juan").apellidos("Perez").build();
    private Usuario usuario2 = new UsuarioFabrica().with().documentoId(456L).nombres("Ana").apellidos("Gomez").build();

    private List<Solicitud> solicitudes = List.of(
            new SolicitudFabrica().with().solicitudId(1L).documentoId(123L).monto(BigDecimal.valueOf(100000)).build(),
            new SolicitudFabrica().with().solicitudId(2L).documentoId(123L).monto(BigDecimal.valueOf(200000)).build(),
            new SolicitudFabrica().with().solicitudId(3L).documentoId(456L).monto(BigDecimal.valueOf(50000)).build()
        );

    private Integer estadoId = 1;
    private Integer limit = 10;
    private Integer offset = 0;

    @Test
    @DisplayName("Debería obtener solicitudes por estado y construir la paginación correctamente")
    void ejecutar_shouldReturnCorrectPaginacion() {
        when(estadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estado));
        when(solicitudRepository.contarSolicitudesPorEstado(anyInt())).thenReturn(Mono.just(3L));
        when(solicitudRepository.obtenerSolicitudesPorEstado(anyInt(), anyInt(), anyInt())).thenReturn(Flux.fromIterable(solicitudes));
        when(usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(123L)).thenReturn(Mono.just(usuario1));
        when(usuarioRestConsumerGateway.obtenerUsuarioPorDocumentoId(456L)).thenReturn(Mono.just(usuario2));
        when(tipoPrestamoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(tipoPrestamo));

        Mono<Paginacion<DetallesSolicitudes>> result = obtenerSolicitudesPorEstadoUseCase.ejecutar(estadoId, limit, offset);

        StepVerifier.create(result)
                .expectNextMatches(paginacion -> {
                    boolean paginacionOk = paginacion.getTotalElementos() == 3L &&
                            paginacion.getTotalPaginas() == 1L &&
                            paginacion.getNumeroPagina() == 1;

                    boolean detallesOk = paginacion.getItems().size() == 2;
                    if (detallesOk) {
                        DetallesSolicitudes detalles1 = paginacion.getItems().stream()
                                .filter(d -> d.getDocumentoId().equals(123L))
                                .findFirst()
                                .orElse(null);

                        DetallesSolicitudes detalles2 = paginacion.getItems().stream()
                                .filter(d -> d.getDocumentoId().equals(456L))
                                .findFirst()
                                .orElse(null);

                        boolean usuario1Ok = detalles1 != null &&
                                detalles1.getNombresUsuario().equals("Juan") &&
                                detalles1.getSolicitudes().size() == 2;

                        boolean usuario2Ok = detalles2 != null &&
                                detalles2.getNombresUsuario().equals("Ana") &&
                                detalles2.getSolicitudes().size() == 1;

                        return paginacionOk && usuario1Ok && usuario2Ok;
                    }
                    return false;
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Debería retornar una paginación vacía cuando no hay solicitudes")
    void ejecutar_shouldReturnEmptyPaginacionWhenNoSolicitudes() {
        when(estadoRepository.obtenerPorId(anyLong())).thenReturn(Mono.just(estado));
        when(solicitudRepository.contarSolicitudesPorEstado(anyInt())).thenReturn(Mono.just(0L));
        when(solicitudRepository.obtenerSolicitudesPorEstado(anyInt(), anyInt(), anyInt())).thenReturn(Flux.empty());

        Mono<Paginacion<DetallesSolicitudes>> result = obtenerSolicitudesPorEstadoUseCase.ejecutar(estadoId, limit, offset);

        StepVerifier.create(result)
                .expectNextMatches(paginacion ->
                        paginacion.getTotalElementos() == 0L &&
                                paginacion.getItems().isEmpty()
                )
                .expectComplete()
                .verify();
    }

}
