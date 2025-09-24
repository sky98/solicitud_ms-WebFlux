package co.com.pragma.api.router;

import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.api.handlers.AccessDeniedHandler;
import co.com.pragma.api.handlers.AuthenticationEntryPoint;
import co.com.pragma.api.handlers.GlobalExceptionHandler;
import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
@RequiredArgsConstructor
public class ObtenerSolicitudesAprobadasPorFechaRouter {

    public static final String PATH = "/api/v1/solicitudes";
    private final Handler handler;

    @Bean
    @RouterOperation(
            path = PATH,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            },
            method = GET,
            beanClass = Handler.class,
            beanMethod = "obtenerSolicitudesAprobadasPorFecha",
            operation = @Operation(
                    operationId = "obtenerSolicitudesAprobadasPorFecha",
                    tags = {"Solicitudes"},
                    summary = "Obtiene las solicitudes aprobadas en un rango de fechas",
                    description = "Requiere el rol '1' (administrador) para acceder a este recurso.",
                    security = @SecurityRequirement(name = "Bearer Authentication"),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Solicitudes obtenidas exitosamente",
                                    content = @Content(schema = @Schema(implementation = SolicitudResponse.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Peticion invalida o error en los datos",
                                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.class))
                            ),
                            @ApiResponse(
                                    responseCode = "401",
                                    description = "No autorizado, el usuario no ha enviado un token de autenticación o el token es inválido.",
                                    content = @Content(schema = @Schema(implementation = AuthenticationEntryPoint.class))
                            ),
                            @ApiResponse(
                                    responseCode = "403",
                                    description = "Acceso denegado, el usuario no tiene los permisos necesarios (rol 'empleado').",
                                    content = @Content(schema = @Schema(implementation = AccessDeniedHandler.class))
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> obtenerSolicitudesAprobadasPorFechaRouterFunction() {
        return RouterFunctions.route(GET(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::obtenerSolicitudesAprobadasPorFecha);
    }
}
