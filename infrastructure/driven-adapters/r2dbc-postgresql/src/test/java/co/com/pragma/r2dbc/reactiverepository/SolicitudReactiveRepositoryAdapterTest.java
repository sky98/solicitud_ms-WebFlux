package co.com.pragma.r2dbc.reactiverepository;

import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.r2dbc.reactiverepository.solicitud.SolicitudReactiveRepository;
import co.com.pragma.r2dbc.reactiverepository.solicitud.SolicitudReactiveRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SolicitudReactiveRepositoryAdapterTest {

    @Mock
    private SolicitudReactiveRepository mockRepository;

    @Mock
    private ObjectMapper mockMapper;

    @Mock
    private TransactionalOperator mockTransactionalOperator;

    @InjectMocks
    private SolicitudReactiveRepositoryAdapter adapter;

    private Solicitud solicitudDominio = Solicitud.builder()
            .solicitudId(1L)
            .documentoId(123L)
            .estadoId(1L)
            .monto(new BigDecimal("50000"))
            .build();

    @Test
    void testGuardar_HappyPath_ShouldReturnMappedObject() {
        when(mockTransactionalOperator.execute(any()))
                .thenReturn(Flux.just(solicitudDominio));

        Mono<Solicitud> result = adapter.guardar(solicitudDominio);

        StepVerifier.create(result)
                .expectNext(solicitudDominio)
                .verifyComplete();
    }

    @Test
    void testGuardar_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockTransactionalOperator.execute(any()))
                .thenReturn(Flux.error(new RuntimeException("Error en la DB")));

        Mono<Solicitud> result = adapter.guardar(solicitudDominio);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().contains("Se ha generado un error al guardar en la tabla solicitud")
                )
                .verify();
    }

    @Test
    void testObtenerSolicitudPorId_HappyPath_ShouldReturnMappedObject() {
        when(mockRepository.findBySolicitudId(anyLong()))
                .thenReturn(Mono.just(solicitudDominio));

        Mono<Solicitud> result = adapter.obtenerSolicitudPorId(1L);

        StepVerifier.create(result)
                .expectNext(solicitudDominio)
                .verifyComplete();
    }

    @Test
    void testObtenerSolicitudPorId_NotFound_ShouldReturnEmptyMono() {
        when(mockRepository.findBySolicitudId(anyLong()))
                .thenReturn(Mono.empty());

        Mono<Solicitud> result = adapter.obtenerSolicitudPorId(99L);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testObtenerSolicitudPorId_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockRepository.findBySolicitudId(anyLong()))
                .thenReturn(Mono.error(new RuntimeException("Error de conexión")));

        Mono<Solicitud> result = adapter.obtenerSolicitudPorId(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().contains("Error al consultar solicitud por id")
                )
                .verify();
    }

    @Test
    void testObtenerSolicitudesPorEstado_HappyPath_ShouldReturnMappedFlux() {
        when(mockRepository.findByEstadoIdWithPagination(anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.just(solicitudDominio));

        Flux<Solicitud> result = adapter.obtenerSolicitudesPorEstado(1, 10, 0);

        StepVerifier.create(result)
                .expectNext(solicitudDominio)
                .verifyComplete();
    }

    @Test
    void testObtenerSolicitudesPorEstado_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockRepository.findByEstadoIdWithPagination(anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.error(new RuntimeException("Error de conexión")));

        Flux<Solicitud> result = adapter.obtenerSolicitudesPorEstado(1, 10, 0);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().contains("Error al consultar solicitudes por estado")
                )
                .verify();
    }

    @Test
    void testObtenerSolicitudesPorDocumentoIdAprobadas_HappyPath_ShouldReturnMappedFlux() {
        when(mockRepository.findByDocumentoIdAndEstadoId(anyLong(), anyLong()))
                .thenReturn(Flux.just(solicitudDominio));

        Flux<Solicitud> result = adapter.obtenerSolicitudesPorDocumentoIdAprobadas(123L);

        StepVerifier.create(result)
                .expectNext(solicitudDominio)
                .verifyComplete();
    }

    @Test
    void testObtenerSolicitudesPorDocumentoIdAprobadas_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockRepository.findByDocumentoIdAndEstadoId(anyLong(), anyLong()))
                .thenReturn(Flux.error(new RuntimeException("Error de conexión")));

        Flux<Solicitud> result = adapter.obtenerSolicitudesPorDocumentoIdAprobadas(123L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().contains("Error al consultar solicitudes aprobadas")
                )
                .verify();
    }

    @Test
    void testContarSolicitudesPorEstado_HappyPath_ShouldReturnCount() {
        when(mockRepository.contarSolicitudesPorEstado(any(Integer.class)))
                .thenReturn(Mono.just(5L));

        Mono<Long> result = adapter.contarSolicitudesPorEstado(1);

        StepVerifier.create(result)
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void testContarSolicitudesPorEstado_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockRepository.contarSolicitudesPorEstado(any(Integer.class)))
                .thenReturn(Mono.error(new RuntimeException("Error de conexión")));

        Mono<Long> result = adapter.contarSolicitudesPorEstado(1);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().contains("Error al consultar total de solicitudes por estado")
                )
                .verify();
    }

    @Test
    void testRollback_ShouldCallSave() {
        when(mockTransactionalOperator.execute(any()))
                .thenReturn(Flux.just(solicitudDominio));

        Mono<Solicitud> result = adapter.rollback(solicitudDominio);

        StepVerifier.create(result)
                .expectNext(solicitudDominio)
                .verifyComplete();
    }

}
