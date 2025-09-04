package co.com.pragma.api.router;

import co.com.pragma.api.handlers.AccessDeniedHandler;
import co.com.pragma.api.handlers.Handler;
import co.com.pragma.model.Paginacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
@RequiredArgsConstructor
public class ConsultarSolicitudesPorEstadoRouter {

    public static final String PATH = "/api/v1/solicitud";
    private final Handler handler;

    @Bean
    @RouterOperation(
            path = PATH,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            },
            method = RequestMethod.GET,
            beanClass = Handler.class,
            beanMethod = "obtenerSolicitudesPorEstado",
            operation = @Operation(
                    operationId = "obtenerSolicitudesPorEstado",
                    tags = {"Solicitudes"},
                    summary = "Consulta las solicitudes por estado con paginación",
                    description = "Requiere el rol de empleado para acceder a este recurso.",
                    parameters = {
                            @Parameter(in = ParameterIn.QUERY, name = "estadoId", description = "ID del estado de la solicitud", required = true),
                            @Parameter(in = ParameterIn.QUERY, name = "limit", description = "Número de solicitudes por página", required = true),
                            @Parameter(in = ParameterIn.QUERY, name = "offset", description = "Desplazamiento para la paginación", required = true)
                    },
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Consulta exitosa",
                                    content = @Content(schema = @Schema(implementation = Paginacion.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Parámetros de consulta inválidos o estado no encontrado",
                                    content = @Content(schema = @Schema(implementation = Exception.class))
                            ),
                            @ApiResponse(
                                    responseCode = "403",
                                    description = "Acceso denegado, el usuario no tiene los permisos necesarios",
                                    content = @Content(schema = @Schema(implementation = AccessDeniedHandler.class))
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> consultarSolicitudesPorEstadoRouterFunction() {
        return RouterFunctions.route(GET(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::obtenerSolicitudesPorEstado);
    }

}
