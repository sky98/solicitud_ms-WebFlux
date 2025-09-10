package co.com.pragma.consecuencias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ActualizarEstadoSolicitudMensaje {
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    private String estado;
    private String tipoPrestamo;
    private Long documentoId;
}
