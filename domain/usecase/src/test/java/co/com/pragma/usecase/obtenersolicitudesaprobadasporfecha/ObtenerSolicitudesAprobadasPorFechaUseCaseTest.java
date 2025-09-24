package co.com.pragma.usecase.obtenersolicitudesaprobadasporfecha;

import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObtenerSolicitudesAprobadasPorFechaUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private ObtenerSolicitudesAprobadasPorFechaUseCase obtenerSolicitudesAprobadasPorFechaUseCase;

    @Test
    @DisplayName("Debería retornar una lista de solicitudes cuando se encuentran registros")
    void ejecutar_shouldReturnApprovedSolicitudes() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fechaFin = LocalDateTime.now();
        List<Solicitud> solicitudesMock = List.of(
                new SolicitudFabrica().with().solicitudId(1L).monto(BigDecimal.TEN).fechaAprobacion(fechaInicio.plusDays(1)).build(),
                new SolicitudFabrica().with().solicitudId(2L).monto(BigDecimal.TEN).fechaAprobacion(fechaInicio.plusDays(5)).build()
        );

        when(solicitudRepository.obtenerSolicitudesAprobadaPorFecha(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.fromIterable(solicitudesMock));

        StepVerifier.create(obtenerSolicitudesAprobadasPorFechaUseCase.ejecutar(fechaInicio, fechaFin))
                .expectNextMatches(solicitudes -> solicitudes.size() == 2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería retornar una lista vacía cuando no hay solicitudes aprobadas en el rango de fechas")
    void ejecutar_shouldReturnEmptyListWhenNoSolicitudes() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(solicitudRepository.obtenerSolicitudesAprobadaPorFecha(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.empty());

        StepVerifier.create(obtenerSolicitudesAprobadasPorFechaUseCase.ejecutar(fechaInicio, fechaFin))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

}
