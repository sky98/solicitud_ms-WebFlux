package co.com.pragma.usecase.obtenersolicitudesaprobadasporfecha;

import co.com.pragma.fabricas.SolicitudFabrica;
import co.com.pragma.fabricas.TipoPrestamoFabrica;
import co.com.pragma.fabricas.UsuarioFabrica;
import co.com.pragma.model.reportediario.ReporteDiario;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObtenerSolicitudesAprobadasPorFechaUseCaseTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private TipoPrestamoRepository tipoPrestamoRepository;

    @Mock
    private UsuarioResConsumerGateway usuarioResConsumerGateway;

    @InjectMocks
    private ObtenerSolicitudesAprobadasPorFechaUseCase obtenerSolicitudesAprobadasPorFechaUseCase;

    private TipoPrestamo tipoPrestamo1 = new TipoPrestamoFabrica()
            .with()
            .tipoPrestamoId(1001L)
            .nombre("Hipoteca")
            .tasaInteres(BigDecimal.valueOf(0.05))
            .build();

    private TipoPrestamo tipoPrestamo2 = new TipoPrestamoFabrica()
            .with()
            .tipoPrestamoId(1002L)
            .nombre("Vehículo")
            .tasaInteres(BigDecimal.valueOf(0.07))
            .build();

    private Usuario usuario1 = new UsuarioFabrica()
            .with()
            .documentoId(101L)
            .nombres("Juan")
            .apellidos("Perez")
            .correoElectronico("juan.perez@example.com")
            .salarioBase(BigDecimal.valueOf(3000000))
            .build();

    private Usuario usuario2 = new UsuarioFabrica()
            .with()
            .documentoId(102L)
            .nombres("Ana")
            .apellidos("Gomez")
            .correoElectronico("ana.gomez@example.com")
            .salarioBase(BigDecimal.valueOf(4500000))
            .build();

    private List<Solicitud> solicitudesMock = List.of(
            new SolicitudFabrica().with().solicitudId(1L).documentoId(101L).tipoPrestamoId(1001L).monto(BigDecimal.valueOf(1000000)).build(),
            new SolicitudFabrica().with().solicitudId(2L).documentoId(102L).tipoPrestamoId(1002L).monto(BigDecimal.valueOf(2500000)).build()
    );


    @Test
    @DisplayName("Debería retornar una lista de ReporteDiario cuando se encuentran registros")
    void ejecutar_shouldReturnApprovedSolicitudes() {
        LocalDateTime fechaInicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fechaFin = LocalDateTime.now();

        when(solicitudRepository.obtenerSolicitudesAprobadaPorFecha(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Flux.fromIterable(solicitudesMock));
        when(usuarioResConsumerGateway.obtenerUsuarioPorDocumentoId(101L)).thenReturn(Mono.just(usuario1));
        when(usuarioResConsumerGateway.obtenerUsuarioPorDocumentoId(102L)).thenReturn(Mono.just(usuario2));
        when(tipoPrestamoRepository.obtenerPorId(1001L)).thenReturn(Mono.just(tipoPrestamo1));
        when(tipoPrestamoRepository.obtenerPorId(1002L)).thenReturn(Mono.just(tipoPrestamo2));

        StepVerifier.create(obtenerSolicitudesAprobadasPorFechaUseCase.ejecutar(fechaInicio, fechaFin))
                .expectNextMatches(reportes -> {
                    if (reportes.size() != 2) return false;

                    ReporteDiario reporte1 = reportes.get(0);
                    boolean isReporte1Correct = reporte1.getDocumentoId().equals(101L) &&
                            reporte1.getNombresUsuario().equals("Juan") &&
                            reporte1.getSolicitud().get(0).getTipoPrestamo().equals("Hipoteca");

                    ReporteDiario reporte2 = reportes.get(1);
                    boolean isReporte2Correct = reporte2.getDocumentoId().equals(102L) &&
                            reporte2.getNombresUsuario().equals("Ana") &&
                            reporte2.getSolicitud().get(0).getTipoPrestamo().equals("Vehículo");

                    return isReporte1Correct && isReporte2Correct;
                })
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
