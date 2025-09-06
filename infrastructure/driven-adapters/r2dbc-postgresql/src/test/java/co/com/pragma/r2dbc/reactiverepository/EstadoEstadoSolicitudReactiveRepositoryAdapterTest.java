package co.com.pragma.r2dbc.reactiverepository;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.r2dbc.entity.SolicitudEntity;
import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.reactiverepository.solicitud.SolicitudReactiveRepository;
import co.com.pragma.r2dbc.reactiverepository.solicitud.SolicitudReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EstadoEstadoSolicitudReactiveRepositoryAdapterTest {

    @Mock
    private SolicitudReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private SolicitudReactiveRepositoryAdapter adapter;

    private Solicitud solicitud;
    private SolicitudEntity solicitudEntity;

    @BeforeEach
    void setUp() {
        solicitud = Solicitud.builder()
                .solicitudId(1L)
                .monto(BigDecimal.ONE)
                .plazo(1L)
                .estadoId(1L)
                .documentoId(1234567L)
                .tipoPrestamoId(1L)
                .build();
        solicitudEntity = SolicitudEntity.builder()
                .solicitudId(1L)
                .monto(BigDecimal.ONE)
                .plazo(1L)
                .estadoId(1L)
                .documentoId(1234567L)
                .tipoPrestamoId("1L")
                .build();
    }

    @Test
    @DisplayName("Debe guardar una solicitud y retornar un Mono<Solicitud> con éxito")
    void guardar_happyPath() {
        when(repository.save(any(SolicitudEntity.class))).thenReturn(Mono.just(solicitudEntity));
        when(mapper.map(any(Solicitud.class), any())).thenReturn(solicitudEntity);
        when(mapper.map(any(SolicitudEntity.class), any())).thenReturn(solicitud);

        when(transactionalOperator.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> transactionCallback = invocation.getArgument(0);
                    return Flux.from(transactionCallback.doInTransaction(null));
                });

        Mono<Solicitud> result = adapter.guardar(solicitud);

        StepVerifier.create(result)
                .expectNextMatches(savedSolicitud -> savedSolicitud.getSolicitudId().equals(1L))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar un Mono<ErrorPersistencia> cuando el guardado falla")
    void guardar_errorPath() {
        when(transactionalOperator.execute(any())).thenReturn(Flux.error(new RuntimeException("Error en la base de datos")));

        Mono<Solicitud> result = adapter.guardar(solicitud);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof ErrorPersistencia && e.getMessage().contains("Se ha generado un error al guardar en la tabla solicitud"))
                .verify();
    }

}
