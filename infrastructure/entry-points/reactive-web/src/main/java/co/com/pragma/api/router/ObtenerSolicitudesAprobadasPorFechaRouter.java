package co.com.pragma.api.router;

import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.api.handlers.GlobalExceptionHandler;
import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    public static final String PATH = "/api/v1/solicitudes/aprobadas/fecha";
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
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> obtenerSolicitudesAprobadasPorFechaRouterFunction() {
        return RouterFunctions.route(GET(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::obtenerSolicitudesAprobadasPorFecha);
    }
}
