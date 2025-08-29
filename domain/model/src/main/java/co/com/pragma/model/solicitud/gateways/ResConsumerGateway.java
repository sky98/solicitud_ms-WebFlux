package co.com.pragma.model.solicitud.gateways;

import reactor.core.publisher.Mono;

public interface ResConsumerGateway {
    Mono<Boolean> validarUsuarioPorDocumentoId(Long documentoId);
}
