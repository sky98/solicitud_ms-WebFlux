package co.com.pragma.sqs.sender.senders;

import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSSenderTest {

    @Mock
    private SQSSenderProperties mockProperties;

    @Mock
    private SqsAsyncClient mockClient;

    @InjectMocks
    private SQSSender sqsSender;

    private static final String QUEUE_URL = "http://localhost:4566/000000000000/";
    private static final String QUEUE_NAME = "mi-cola";
    private static final String MESSAGE_ID = "abc-123-xyz";

    @BeforeEach
    void setUp() {
        when(mockProperties.queueUrl()).thenReturn(QUEUE_URL);
    }

    @Test
    @DisplayName("Test exitoso: El mensaje se envía correctamente")
    void testSend_HappyPath_ShouldReturnMessageId() {
        SendMessageResponse mockResponse = SendMessageResponse.builder()
                .messageId(MESSAGE_ID)
                .build();
        when(mockClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        Mono<String> result = sqsSender.send("Test message", QUEUE_NAME);

        StepVerifier.create(result)
                .expectNext(MESSAGE_ID)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test de error: El cliente de SQS falla al enviar el mensaje")
    void testSend_ClientError_ShouldReturnError() {
        when(mockClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("SQS client error")));

        Mono<String> result = sqsSender.send("Test message", QUEUE_NAME);

        StepVerifier.create(result)
                .expectError()
                .verify();
    }
}
