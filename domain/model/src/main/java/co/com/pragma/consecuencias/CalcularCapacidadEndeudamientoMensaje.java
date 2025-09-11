package co.com.pragma.consecuencias;

import co.com.pragma.model.solicitud.Solicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CalcularCapacidadEndeudamientoMensaje {
    private BigDecimal monto;
    private Long plazo;
    private BigDecimal salarioBase;
    private BigDecimal tasaInteresMensual;
    private Long tipoPrestamoId;
    private Long documentoId;
    private List<Solicitud> solicitudes;
}
