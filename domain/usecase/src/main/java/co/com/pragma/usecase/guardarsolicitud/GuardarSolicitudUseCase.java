package co.com.pragma.usecase.guardarsolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class GuardarSolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public Mono<Solicitud> ejecutar(Solicitud solicitud, String prestamoNombre){
        return tipoPrestamoRepository.existePorNombre(prestamoNombre)
                .flatMap(
                        tipoPrestamo -> tipoPrestamoRepository.existeMontoEnRango(tipoPrestamo.getTipoPrestamoId(), solicitud.getMonto())
                ).flatMap(cumpleMonto -> cumpleMonto
                        ? solicitudRepository.guardar(solicitud)
                        : Mono.error(new ErrorDominio("Monto no cumnple con el rango del tipo de prestamo", Set.of("monto")))
                );
    }
}
