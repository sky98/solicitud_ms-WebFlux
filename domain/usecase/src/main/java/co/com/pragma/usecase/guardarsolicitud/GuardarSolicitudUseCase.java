package co.com.pragma.usecase.guardarsolicitud;

import co.com.pragma.errores.ErrorDominio;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.ResConsumerGateway;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public class GuardarSolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ResConsumerGateway resConsumerGateway;

    public Mono<Solicitud> ejecutar(Solicitud solicitud, String usuarioAutenticado){
        return Mono.defer(() -> {
                    if (!String.valueOf(solicitud.getDocumentoId()).equals(usuarioAutenticado)) {
                        return Mono.error(new ErrorDominio(
                                "El documento de la solicitud no coincide con el documento del usuario autenticado.",
                                Set.of("usuario:documentoId")));
                    }
                    return Mono.just(solicitud);
                })
                .flatMap(validatedSolicitud ->
                        resConsumerGateway.validarUsuarioPorDocumentoId(solicitud.getDocumentoId())
                                .flatMap(usuarioExiste -> Boolean.TRUE.equals(usuarioExiste)
                                        ? gestionInterna(solicitud)
                                        : Mono.error(new ErrorDominio(
                                        String.format("Usuario con documentoId: %d no existe en el sistema", solicitud.getDocumentoId()),
                                        Set.of("usuario:documentoId"))))
                );
    }

    private Mono<Solicitud> gestionInterna(Solicitud solicitud){
        return tipoPrestamoRepository.obtenerPorId(solicitud.getTipoPrestamoId())
                .switchIfEmpty(Mono.error(new ErrorDominio("El tipo de prestamo no existe", Set.of("tipoPrestamoId"))))
                .flatMap(
                        tipoPrestamo -> tipoPrestamoRepository.existeMontoEnRango(tipoPrestamo.getTipoPrestamoId(), solicitud.getMonto())
                ).flatMap(cumpleMonto -> cumpleMonto
                        ? solicitudRepository.guardar(solicitud)
                        : Mono.error(new ErrorDominio("Monto no cumnple con el rango del tipo de prestamo", Set.of("monto")))
                );
    }
}
