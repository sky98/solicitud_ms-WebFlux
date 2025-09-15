package co.com.pragma.sqs.sender.senders;

import co.com.pragma.errores.ErrorSQS;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender {

    private final ObjectMapper objectMapper;
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    public  <T> Mono<String> serializar(T object){
        String data = null;
        try{
            data = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e){
            log.error("Error en el proceso de serialización. Error: {}",e.getMessage());
            return Mono.error(new ErrorSQS("Error en el proceso de serialización. Error: " + e.getMessage(), Set.of(e.getMessage())));
        }
        return Mono.just(data);
    }
}
