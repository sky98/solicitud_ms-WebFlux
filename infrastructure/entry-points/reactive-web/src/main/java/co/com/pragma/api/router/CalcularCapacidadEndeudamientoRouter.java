package co.com.pragma.api.router;

import co.com.pragma.api.dto.request.CalcularCapacidadEndeudamientoRequestDTO;
import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.api.handlers.AccessDeniedHandler;
import co.com.pragma.api.handlers.AuthenticationEntryPoint;
import co.com.pragma.api.handlers.GlobalExceptionHandler;
import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class CalcularCapacidadEndeudamientoRouter {

    public static final String PATH = "/api/v1/calcular-capacidad";
    private final Handler handler;

    @Bean
    @RouterOperation(
            path = PATH,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            },
            method = RequestMethod.POST,
            beanClass = Handler.class,
            beanMethod = "calcularCapacidadEndeudamiento",
            operation = @Operation(
                    operationId = "calcularCapacidadEndeudamiento",
                    tags = {"Solicitudes"},
                    summary = "Calcula la capacidad de endeudamiento de una solicitud.",
                    description = "Requiere un token de autenticación (Bearer Token) para acceder a este recurso. Solo los usuarios con el rol 'empleado' (ID 2) pueden utilizarlo.",
                    requestBody = @RequestBody(
                            content = @Content(schema = @Schema(implementation = CalcularCapacidadEndeudamientoRequestDTO.class)),
                            required = true
                    ),
                    security = @SecurityRequirement(name = "Bearer Authentication"),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Cálculo de capacidad de endeudamiento exitoso",
                                    content = @Content(schema = @Schema(implementation = SolicitudResponse.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Solicitud inválida o errores de validación",
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
                            ),
                            @ApiResponse(
                                    responseCode = "404",
                                    description = "Solicitud no encontrada",
                                    content = @Content(schema = @Schema(implementation = GlobalExceptionHandler.class))
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> calcularCapacidadEndeudamientoRouterFunction() {
        return RouterFunctions.route(POST(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::calcularCapacidadEndeudamiento);
    }

}
