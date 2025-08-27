package co.com.pragma.api.handlers;

import co.com.pragma.api.dto.request.CrearSolicitudDTO;
import co.com.pragma.api.mapper.SolicitudDTOMapper;
import co.com.pragma.api.validador.ValidadorRequest;
import co.com.pragma.usecase.guardarsolicitud.GuardarSolicitudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final ValidadorRequest validadorRequest;
    private final SolicitudDTOMapper mapper;
    private final GuardarSolicitudUseCase useCase;

    public Mono<ServerResponse> guardarSolicitud(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CrearSolicitudDTO.class)
                .doOnNext(requestDto -> log.info("Iniciando flujo de guardar solicitud : {}", requestDto))
                .flatMap(validadorRequest::validar)
                .map(dto -> useCase.ejecutar(mapper.toModel(dto), dto.nombreTipoPrestamo()))

    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        // useCase2.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }
}
