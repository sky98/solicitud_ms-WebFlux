package co.com.pragma.fabricas;

import co.com.pragma.model.solicitud.MensajeProcesadoSolicitud;

public class MensajeProcesadoSolicitudFabrica {

    private MensajeProcesadoSolicitud.MensajeProcesadoSolicitudBuilder builder;

    public MensajeProcesadoSolicitudFabrica(){
        this.builder = MensajeProcesadoSolicitud.builder()
                .solicitudId(1)
                .estado("APROBADO");
    }

    public static MensajeProcesadoSolicitudFabrica builder(){
        return new MensajeProcesadoSolicitudFabrica();
    }

    public MensajeProcesadoSolicitud.MensajeProcesadoSolicitudBuilder with(){
        return this.builder;
    }

    public MensajeProcesadoSolicitud build(){
        return this.builder.build();
    }
}
