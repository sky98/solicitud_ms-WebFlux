package co.com.pragma.sqs.sender.mapper;

import co.com.pragma.errores.ErrorSQS;
import co.com.pragma.model.mensaje.gateways.MensajeUtilsGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapperMensajesUtils implements MensajeUtilsGateway {

    private final ObjectMapper objectMapper;

    @Override
    public <T> Mono<String> serializar(T object) {
        String data;
        try{
            data = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e){
            log.error("Error en el proceso de serialización. Error: {}",e.getMessage());
            return Mono.error(new ErrorSQS("Error en el proceso de serialización. Error: " + e.getMessage(), Set.of(e.getMessage())));
        }
        return Mono.just(data);
    }

    @Override
    public <T> Mono<T> deserializarMensaje(String messageBody, Class<T> targetClass) {
        log.info("DEserializando : {}", messageBody);
        return Mono.fromCallable(() -> {
            try {
                return objectMapper.readValue(messageBody, targetClass);
            } catch (Exception e) {
                throw new IllegalStateException("Error al parsear el JSON " + targetClass.getName(), e);
            }
        });
    }
}
