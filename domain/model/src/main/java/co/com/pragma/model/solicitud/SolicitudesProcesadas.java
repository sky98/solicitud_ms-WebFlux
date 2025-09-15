package co.com.pragma.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudesProcesadas {
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    private String estado;
    private String tipoPrestamo;
    private BigDecimal tasaInteres;
}
