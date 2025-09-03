package co.com.pragma.api.router;

import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Operaciones relacionadas con las solicitudes de los usuarios")
public class GuardarSolicitudRouter {

    public static final String PATH = "/api/v1/solicitud";
    private final Handler handler;

    @Bean
    @RouterOperation(
            path = PATH,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            },
            method = RequestMethod.POST,
            beanClass = Handler.class,
            beanMethod = "guardarSolicitud",
            operation = @Operation(
                    operationId = "guardarSolicitud",
                    tags = {"Solicitudes"},
                    summary = "Guarda una nueva solicitud",
                    description = "Requiere el rol de cliente para acceder a este recurso.",
                    requestBody = @RequestBody(
                            description = "Información de la solicitud a guardar",
                            required = true,
                            content = @Content(
                                    schema = @Schema(implementation = CrearSolicitudRequestDTO.class)
                            )
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "201",
                                    description = "Solicitud guardada exitosamente",
                                    content = @Content(schema = @Schema(implementation = SolicitudResponse.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Solicitud inválida o error en los datos",
                                    content = @Content(schema = @Schema(implementation = Exception.class))
                            ),
                            @ApiResponse(
                                    responseCode = "403",
                                    description = "Acceso denegado, el usuario no tiene los permisos necesarios",
                                    content = @Content(schema = @Schema(implementation = Exception.class))
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> guardarSolicitudRouterFunction() {
        return RouterFunctions.route(POST(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::guardarSolicitud);
    }

}
