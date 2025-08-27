package co.com.pragma.api.router;

import co.com.pragma.api.handlers.Handler;
import co.com.pragma.api.errores.ErrorValidacion;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GuardarSolicitudRouter {

    public static final String PATH = "/api/v1/solicitudes";
    private final Handler handler;

    @Bean
    @RouterOperation(
            path = PATH,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            },
            method = org.springframework.web.bind.annotation.RequestMethod.POST,
            beanClass = Handler.class,
            beanMethod = "guardarUsuario",
            operation = @Operation(
                    operationId = "guardarUsuario",
                    tags = {"Usuarios"},
                    summary = "Guarda un nuevo usuario",
                    requestBody = @RequestBody(
                            description = "Información del usuario a guardar",
                            required = true,
                            content = @Content(
                                    schema = @Schema(implementation = CrearUsuarioDTO.class)
                            )
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "200",
                                    description = "Usuario guardado exitosamente",
                                    content = @Content(schema = @Schema(implementation = UsuarioDTO.class))
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Solicitud inválida",
                                    content = @Content(
                                            schema = @Schema(implementation = ErrorValidacion.class),
                                            examples = @ExampleObject(
                                                    name = "Ejemplo de respuesta de error 400",
                                                    value = "{\"status\": 400,\"message\": \"Correo no esta disponible\",\"errors\": [\"correoElectronico\"]}"
                                            )
                                    )
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> usuarioRouterFunction() {
        return RouterFunctions.route(POST(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::guardarUsuario);
    }

}
