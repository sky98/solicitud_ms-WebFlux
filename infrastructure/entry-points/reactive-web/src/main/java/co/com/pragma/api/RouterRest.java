package co.com.pragma.api;

import co.com.pragma.api.handlers.Handler;
import co.com.pragma.api.router.ActualizarEstadoSolicitudRouter;
import co.com.pragma.api.router.CalcularCapacidadEndeudamientoRouter;
import co.com.pragma.api.router.CheckHealthRouter;
import co.com.pragma.api.router.ConsultarSolicitudesPorEstadoRouter;
import co.com.pragma.api.router.GuardarSolicitudRouter;
import co.com.pragma.api.router.ObtenerSolicitudesAprobadasPorFechaRouter;
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
    private final ActualizarEstadoSolicitudRouter actualizarEstadoSolicitudRouter;
    private final CalcularCapacidadEndeudamientoRouter calcularCapacidadEndeudamientoRouter;
    private final ObtenerSolicitudesAprobadasPorFechaRouter obtenerSolicitudesAprobadasPorFechaRouter;
    private final CheckHealthRouter checkHealthRouter;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return RouterFunctions.route()
                .add(guardarSolicitudRouter.guardarSolicitudRouterFunction())
                .add(consultarSolicitudesPorEstadoRouter.consultarSolicitudesPorEstadoRouterFunction())
                .add(actualizarEstadoSolicitudRouter.actualizarEstadoSolicitudRouterFunction())
                .add(calcularCapacidadEndeudamientoRouter.calcularCapacidadEndeudamientoRouterFunction())
                .add(obtenerSolicitudesAprobadasPorFechaRouter.obtenerSolicitudesAprobadasPorFechaRouterFunction())
                .add(checkHealthRouter.checkHealthRouterFunction())
                .build();
    }
}
