package co.com.pragma.sqs.sender.mensajes;

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
public class SolicitudLite {
    private BigDecimal monto;
    private Long plazo;
    private BigDecimal tasaInteres;
}
