package co.com.pragma.fabricas;

import co.com.pragma.model.solicitud.Solicitud;

import java.math.BigDecimal;

public class SolicitudFabrica {

    private Solicitud.SolicitudBuilder builder;

    public SolicitudFabrica(){
        this.builder = Solicitud.builder()
                .solicitudId(1L)
                .monto(BigDecimal.valueOf(50000))
                .plazo(12L)
                .estadoId(1L)
                .tipoPrestamoId(1L)
                .documentoId(1233456L);
    }

    public static SolicitudFabrica builder(){
        return new SolicitudFabrica();
    }

    public Solicitud.SolicitudBuilder with(){
        return this.builder;
    }

    public Solicitud build(){
        return this.builder.build();
    }
}
