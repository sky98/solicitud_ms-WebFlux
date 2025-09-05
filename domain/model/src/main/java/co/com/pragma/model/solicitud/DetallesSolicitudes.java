package co.com.pragma.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

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
    private List<SolicitudResponse> solicitudes;
    private BigDecimal deudaTotalMensualSolicitudesAprobadas;
}
