package co.com.pragma.fabricas;

import co.com.pragma.model.estado.Estado;

public class EstadoFabrica {

    private Estado.EstadoBuilder builder;

    public EstadoFabrica(){
        this.builder = Estado.builder()
                .estadoId(1L)
                .nombre("Pendiente de revision")
                .descripcion("La solicitud ha sido recibida y está en espera de revisión.");
    }

    public static EstadoFabrica builder(){
        return new EstadoFabrica();
    }

    public Estado.EstadoBuilder with(){
        return this.builder;
    }

    public Estado build(){
        return this.builder.build();
    }
}
