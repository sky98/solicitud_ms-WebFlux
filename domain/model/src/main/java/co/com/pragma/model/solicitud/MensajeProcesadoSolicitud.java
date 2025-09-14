package co.com.pragma.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MensajeProcesadoSolicitud {
    private int solicitudId;
    private String estado;
}
