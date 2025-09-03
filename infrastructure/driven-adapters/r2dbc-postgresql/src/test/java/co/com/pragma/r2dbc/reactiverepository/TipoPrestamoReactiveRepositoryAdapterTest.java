package co.com.pragma.r2dbc.reactiverepository;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.r2dbc.entity.TipoPrestamoEntity;
import co.com.pragma.errores.ErrorPersistencia;
import co.com.pragma.r2dbc.reactiverepository.tipoprestamo.TipoPrestamoReactiveRepository;
import co.com.pragma.r2dbc.reactiverepository.tipoprestamo.TipoPrestamoReactiveRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoPrestamoReactiveRepositoryAdapterTest {
    @Mock
    private TipoPrestamoReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private TipoPrestamoReactiveRepositoryAdapter adapter;

    private TipoPrestamo tipoPrestamo;
    private TipoPrestamoEntity tipoPrestamoEntity;
    private final Long TIPO_PRESTAMO_ID = 1L;
    private final BigDecimal MONTO_VALIDO = new BigDecimal("1000000");
    private final String MENSAJE_ERROR_OBTENER_ID = "Error al consultar tipo_prestamo por id, error : ";
    private final String MENSAJE_ERROR_VALIDAR_MONTO = "Error al validar monto en la tabla tipo_prestamo, error : ";

    @BeforeEach
    void setUp() {
        tipoPrestamo = TipoPrestamo.builder().tipoPrestamoId(TIPO_PRESTAMO_ID).nombre("Test").build();
        tipoPrestamoEntity = new TipoPrestamoEntity(TIPO_PRESTAMO_ID, "Test", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "Test");
    }

    @Test
    @DisplayName("Debe retornar un Mono<TipoPrestamo> cuando el id existe")
    void testTipoPrestamo_ObtenerPorId_RetornaMonoTipoPrestamo() {
        when(repository.findById(TIPO_PRESTAMO_ID)).thenReturn(Mono.just(tipoPrestamoEntity));
        when(mapper.map(any(TipoPrestamoEntity.class), any())).thenReturn(tipoPrestamo);

        Mono<TipoPrestamo> result = adapter.obtenerPorId(TIPO_PRESTAMO_ID);

        StepVerifier.create(result)
                .expectNext(tipoPrestamo)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar un Mono<ErrorPersistencia> cuando la búsqueda falla")
    void testTipoPrestamo_ObtenerPorId_RetornaMonoErrorPersistencia() {
        when(repository.findById(TIPO_PRESTAMO_ID)).thenReturn(Mono.error(new RuntimeException("Error en la DB")));

        Mono<TipoPrestamo> result = adapter.obtenerPorId(TIPO_PRESTAMO_ID);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof ErrorPersistencia && e.getMessage().contains(MENSAJE_ERROR_OBTENER_ID))
                .verify();
    }

    @Test
    @DisplayName("Debe retornar un Mono<true> cuando el monto está en el rango")
    void testTipoPrestamo_ValidarRangoMonto_RetornaMonoTrue() {
        when(repository.contarMontoEnRangoPorId(TIPO_PRESTAMO_ID, MONTO_VALIDO)).thenReturn(Mono.just(1L));

        Mono<Boolean> result = adapter.existeMontoEnRango(TIPO_PRESTAMO_ID, MONTO_VALIDO);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar un Mono<false> cuando el monto no está en el rango")
    void testTipoPrestamo_ValidarRangoMonto_RetornaMonoFalse() {
        when(repository.contarMontoEnRangoPorId(TIPO_PRESTAMO_ID, MONTO_VALIDO)).thenReturn(Mono.just(0L));

        Mono<Boolean> result = adapter.existeMontoEnRango(TIPO_PRESTAMO_ID, MONTO_VALIDO);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar un Mono<ErrorPersistencia> cuando la validación falla")
    void testTipoPrestamo_ValidarRangoMonto_RetornaMonoErrorPersistencia() {
        when(repository.contarMontoEnRangoPorId(TIPO_PRESTAMO_ID, MONTO_VALIDO)).thenReturn(Mono.error(new RuntimeException("Error en la DB")));

        Mono<Boolean> result = adapter.existeMontoEnRango(TIPO_PRESTAMO_ID, MONTO_VALIDO);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof ErrorPersistencia && e.getMessage().contains(MENSAJE_ERROR_VALIDAR_MONTO))
                .verify();
    }
}
