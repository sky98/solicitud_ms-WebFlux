package co.com.pragma.sqs.listener;

import co.com.pragma.model.mensaje.gateways.MensajeUtilsGateway;
import co.com.pragma.usecase.procesarcalculocapacidadendeudamiento.ProcesarCalculoCapacidadEndeudamientoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ProcesarCalculoCapacidadEndeudamientoUseCase procesarCalculoCapacidadEndeudamientoUseCase;
    private final MensajeUtilsGateway mensajeUtilsGateway;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Procesando mensaje : {}", message.body());
        return procesarCalculoCapacidadEndeudamientoUseCase.ejecutar(message.body())
                .flatMap(mensajeUtilsGateway::serializar)
                .flatMap(resp -> {
                    log.info("Mensaje procesado con exito : {}", resp);
                    return Mono.empty();
                });
    }
}
