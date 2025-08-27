package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CrearSolicitudDTO(
        @NotBlank(message = "El campo :usuarioDoumentoId es obligatorio")
        Long usuarioDocumentoId,
        @NotBlank(message = "El campo :monto es obligatorio")
        BigDecimal monto,
        @NotBlank(message = "El campo :plazo es obligatorio")
        Long plazo,
        @NotBlank(message = "El campo :nombrePlan es obligatorio")
        String nombreTipoPrestamo
) {
}
