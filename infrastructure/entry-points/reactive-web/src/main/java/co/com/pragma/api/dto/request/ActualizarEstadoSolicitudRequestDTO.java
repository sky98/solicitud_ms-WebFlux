package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoSolicitudRequestDTO(
        @NotBlank(message = "El campo :solicitudId es obligatorio")
        String solicitudId,
        @NotBlank(message = "El campo :estadoId es obligatorio")
        String estadoId
) {
}
