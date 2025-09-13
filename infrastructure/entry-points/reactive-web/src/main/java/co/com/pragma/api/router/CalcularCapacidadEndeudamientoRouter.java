package co.com.pragma.api.router;

import co.com.pragma.api.handlers.Handler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Solicitudes", description = "Operaciones relacionadas con las solicitudes de los usuarios")
public class CalcularCapacidadEndeudamientoRouter {

    public static final String PATH = "/api/v1/calcular-capacidad";
    private final Handler handler;

    @Bean
    public RouterFunction<ServerResponse> calcularCapacidadEndeudamientoRouterFunction() {
        return RouterFunctions.route(POST(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::calcularCapacidadEndeudamiento);
    }

}
