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
public class DetallesSolicitudes {
    private Long documentoId;
    private String correoElectronico;
    private BigDecimal salarioBase;
    private String nombresUsuario;
    private String apellidosUsuario;
    private BigDecimal monto;
    private Long plazo;
    private String estadoSolicitud;
    private String tipoPrestamo;
    private BigDecimal tasaInteresPrestamo;
    private BigDecimal deudaTotalMensualSolicitudesAprobadas;
}
