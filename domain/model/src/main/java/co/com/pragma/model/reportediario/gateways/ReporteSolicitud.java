package co.com.pragma.model.reportediario.gateways;

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
public class ReporteSolicitud {
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    private String tipoPrestamo;
    private BigDecimal tasaInteres;
}
