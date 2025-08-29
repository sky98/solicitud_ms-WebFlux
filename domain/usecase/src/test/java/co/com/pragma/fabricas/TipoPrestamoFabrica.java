package co.com.pragma.fabricas;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;

import java.math.BigDecimal;

public class TipoPrestamoFabrica {

    private TipoPrestamo.TipoPrestamoBuilder builder;

    public TipoPrestamoFabrica() {
        this.builder = TipoPrestamo.builder()
                .tipoPrestamoId(1L)
                .nombre("Prestamo Estandar")
                .montoMinimo(BigDecimal.valueOf(1000000))
                .montoMaximo(BigDecimal.valueOf(5000000))
                .tasaInteres(BigDecimal.valueOf(1.5))
                .validacionAutomatica("SI");
    }

    public static TipoPrestamoFabrica builder() {
        return new TipoPrestamoFabrica();
    }

    public TipoPrestamo.TipoPrestamoBuilder with() {
        return this.builder;
    }

    public TipoPrestamo build() {
        return this.builder.build();
    }

}
