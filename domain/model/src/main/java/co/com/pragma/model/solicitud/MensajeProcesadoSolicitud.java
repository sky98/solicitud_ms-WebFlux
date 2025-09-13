package co.com.pragma.model.solicitud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensajeProcesadoSolicitud {
    private int solicitudId;
    private String estado;
}
