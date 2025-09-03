package co.com.pragma.api.router;

import co.com.pragma.api.handlers.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
    public RouterFunction<ServerResponse> consultarSolicitudesPorEstadoRouterFunction() {
        return RouterFunctions.route(GET(PATH).and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::obtenerSolicitudesPorEstado);
    }

}
