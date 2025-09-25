package co.com.pragma.usecase.calcularcapacidadendeudamiento;

import co.com.pragma.errores.ErrorValidacion;
import co.com.pragma.model.mensaje.gateways.EncolarMensajeGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class CalcularCapacidadEndeudamientoUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EncolarMensajeGateway encolarMensajeGateway;

    public Mono<Solicitud> ejecutar(Long solicitudId){
        return solicitudRepository.obtenerSolicitudPorId(solicitudId)
                .switchIfEmpty(Mono.error(new ErrorValidacion("Solicitud no existe en el sistema : "+solicitudId, Set.of("solicitudId:"+solicitudId))))
                .filter(solicitud -> solicitud.getEstadoId() == 1)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new ErrorValidacion("La solicitud debe estar pendiente de revision para poder ejecutar este comando", Set.of("solicitudId:"+solicitudId)))))
                .flatMap(encolarMensajeGateway::calcularCapacidadEndeudamiento);
    }

}
