package co.com.pragma.model.solicitud;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    private Long estadoId;
    private Long tipoPrestamoId;
    private Long documentoId;
    private LocalDateTime fechaAprobacion;
}
