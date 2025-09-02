package co.com.pragma.api.handlers;

import co.com.pragma.api.dto.UsuarioAutenticado;
import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.api.validador.ValidadorRequest;
import co.com.pragma.usecase.guardarsolicitud.GuardarSolicitudUseCase;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    private final ValidadorRequest validadorRequest;
    private final SolicitudMapper mapper;
    private final GuardarSolicitudUseCase useCase;

    @PreAuthorize("hasRole('3')")
    public Mono<ServerResponse> guardarSolicitud(ServerRequest serverRequest) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    String uid = (String) securityContext.getAuthentication().getPrincipal();
                    Integer rolId = (Integer) securityContext.getAuthentication().getCredentials();
                    return new UsuarioAutenticado(uid, rolId, securityContext.getAuthentication().getAuthorities().stream().map(Object::toString).toList());
                })
                        .flatMap(autenticado -> serverRequest.bodyToMono(CrearSolicitudRequestDTO.class)
                                .doOnNext(requestDto -> log.info("Iniciando flujo de registrar solicitud : {}, usuarioAutenticado : {}", requestDto, autenticado.uid()))
                                .flatMap(validadorRequest::validar)
                                .map(mapper::toModel)
                                .flatMap(solicitud -> useCase.ejecutar(solicitud, autenticado.uid()))
                                .map(mapper::toResponse)
                                .flatMap(resp -> {
                                            log.info("Solicitud registrada con exito : {}", resp);
                                            return ServerResponse.status(HttpResponseStatus.CREATED.code())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .bodyValue(resp);

                                        })
                        )
                .doOnError(e -> log.error("Error en el servicio registrar solicitud : {}", e.getMessage()));

    }
}
