package co.com.pragma.r2dbc.reactiverepository;

import co.com.pragma.model.estado.Estado;
import co.com.pragma.r2dbc.entity.EstadoSolicitudEntity;
import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.reactiverepository.estadosolicitud.EstadoSolicitudReactiveRepository;
import co.com.pragma.r2dbc.reactiverepository.estadosolicitud.EstadoSolicitudReactiveRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EstadoEstadoSolicitudReactiveRepositoryAdapterTest {

    @Mock
    private EstadoSolicitudReactiveRepository mockRepository;

    @Mock
    private ObjectMapper mockMapper;

    @InjectMocks
    private EstadoSolicitudReactiveRepositoryAdapter adapter;

    private Estado estadoDominio = Estado.builder()
            .estadoId(1L)
            .nombre("APROBADO")
            .build();

    @Test
    void testObtenerPorId_HappyPath_ShouldReturnMappedObject() {
        when(mockRepository.findByEstadoId(anyLong()))
                .thenReturn(Mono.just(estadoDominio));

        Mono<Estado> result = adapter.obtenerPorId(1L);

        StepVerifier.create(result)
                .expectNext(estadoDominio)
                .verifyComplete();
    }

    @Test
    void testObtenerPorId_NotFound_ShouldReturnEmptyMono() {
        when(mockRepository.findByEstadoId(anyLong()))
                .thenReturn(Mono.empty());

        Mono<Estado> result = adapter.obtenerPorId(99L);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testObtenerPorId_RepositoryError_ShouldReturnErrorPersistencia() {
        when(mockRepository.findByEstadoId(anyLong()))
                .thenReturn(Mono.error(new RuntimeException("Error de conexión a DB")));

        Mono<Estado> result = adapter.obtenerPorId(1L);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ErrorPersistencia &&
                                throwable.getMessage().equals("Error al obtener estado por id")
                )
                .verify();
    }

}
