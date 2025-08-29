package co.com.pragma.api.router;

import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@RequiredArgsConstructor
public class GuardarSolicitudRouter {

    public static final String PATH = "/api/v1/solicitudes";
    private final Handler handler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/v1/solicitudes",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = org.springframework.web.bind.annotation.RequestMethod.POST,
                            beanClass = Handler.class,
                            beanMethod = "guardarSolicitud",
                            operation = @Operation(
                                    summary = "Guarda una nueva solicitud de préstamo",
                                    description = "Crea una nueva solicitud en el sistema y la retorna con el estado inicial.",
                                    tags = {"Solicitudes"},
                                    requestBody = @RequestBody(
                                            required = true,
                                            description = "Los datos de la solicitud de préstamo a guardar.",
                                            content = @Content(schema = @Schema(implementation = CrearSolicitudRequestDTO.class))
                                    ),
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "201",
                                                    description = "Solicitud creada exitosamente",
                                                    content = @Content(schema = @Schema(implementation = CrearSolicitudRequestDTO.class))
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Petición inválida o error de validación"
                                            )
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> guardarSolicitudRouterFunction() {
        return RouterFunctions.route(POST(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::guardarSolicitud);
    }

}
