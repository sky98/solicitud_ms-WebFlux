package co.com.pragma.model.mensaje.gateways;

import reactor.core.publisher.Mono;

public interface MensajeUtilsGateway {
    <T> Mono<String> serializar(T object);
    <T> Mono<T> deserializarMensaje(String messageBody, Class<T> targetClass);
}
