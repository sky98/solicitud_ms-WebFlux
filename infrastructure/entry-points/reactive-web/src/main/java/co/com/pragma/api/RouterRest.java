package co.com.pragma.api;

import co.com.pragma.api.handlers.Handler;
import co.com.pragma.api.router.ConsultarSolicitudesPorEstadoRouter;
import co.com.pragma.api.router.GuardarSolicitudRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final GuardarSolicitudRouter guardarSolicitudRouter;
    private final ConsultarSolicitudesPorEstadoRouter consultarSolicitudesPorEstadoRouter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return RouterFunctions.route()
                .add(guardarSolicitudRouter.guardarSolicitudRouterFunction())
                .add(consultarSolicitudesPorEstadoRouter.consultarSolicitudesPorEstadoRouterFunction())
                .build();
    }
}
