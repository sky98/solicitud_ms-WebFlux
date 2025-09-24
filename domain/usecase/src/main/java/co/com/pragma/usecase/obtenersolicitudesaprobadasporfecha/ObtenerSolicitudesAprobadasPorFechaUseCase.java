package co.com.pragma.usecase.obtenersolicitudesaprobadasporfecha;

import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ObtenerSolicitudesAprobadasPorFechaUseCase {

    private final SolicitudRepository solicitudRepository;

    public Mono<List<Solicitud>> ejecutar(LocalDateTime fechaInicio, LocalDateTime fechaFin){
        return solicitudRepository.obtenerSolicitudesAprobadaPorFecha(fechaInicio, fechaFin).collectList();
    }

}
