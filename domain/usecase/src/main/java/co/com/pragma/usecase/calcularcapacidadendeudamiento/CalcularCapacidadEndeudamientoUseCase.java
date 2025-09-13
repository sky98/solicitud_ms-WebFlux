package co.com.pragma.usecase.calcularcapacidadendeudamiento;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class CalcularCapacidadEndeudamientoUseCase {

    private final SolicitudRepository solicitudRepository;
    private final MensajeSQSGateway mensajeSQSGateway;

    public Mono<Solicitud> ejecutar(Long solicitudId){
        return solicitudRepository.obtenerSolicitudPorId(solicitudId)
                .switchIfEmpty(Mono.error(new ErrorValidacion("Solicitud no existe en el sistema : "+solicitudId, Set.of("solicitudId:"+solicitudId))))
                .flatMap(mensajeSQSGateway::calcularCapacidadEndeudamiento);
    }

}
