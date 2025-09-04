package co.com.pragma.api.handlers;

import co.com.pragma.api.dto.UsuarioAutenticado;
import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.mapper.SolicitudMapper;
import co.com.pragma.api.validador.ValidadorRequest;
import co.com.pragma.usecase.guardarsolicitud.GuardarSolicitudUseCase;
import co.com.pragma.usecase.obtenersolicitudesporestado.ObtenerSolicitudesPorEstadoUseCase;
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

    private final GuardarSolicitudUseCase guardarSolicitudUseCase;
    private final ObtenerSolicitudesPorEstadoUseCase obtenerSolicitudesPorEstadoUseCase;
    private final ValidadorRequest validadorRequest;
    private final SolicitudMapper mapper;

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
                                .flatMap(solicitud -> guardarSolicitudUseCase.ejecutar(solicitud, autenticado.uid()))
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

    @PreAuthorize("hasRole('2')")
    public Mono<ServerResponse> obtenerSolicitudesPorEstado(ServerRequest serverRequest){
        String estadoId = serverRequest.queryParam("estadoId").orElse("1");
        String limit = serverRequest.queryParam("limit").orElse("10");
        String offset = serverRequest.queryParam("offset").orElse("0");
        log.info("Consultando solicitudes con estadoId : {}", estadoId);
        return obtenerSolicitudesPorEstadoUseCase.ejecutar(Integer.valueOf(estadoId), Integer.valueOf(limit), Integer.valueOf(offset))
                .flatMap(response ->{
                    log.info("Extraccion de solicitudes por estadoId : {}, realizada con exito", estadoId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}
