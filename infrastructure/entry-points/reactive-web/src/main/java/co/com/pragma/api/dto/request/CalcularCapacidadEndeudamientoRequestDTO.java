package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CalcularCapacidadEndeudamientoRequestDTO(
        @NotBlank(message = "El campo :solicitudId es obligatorio")
        String solicitudId
) {
}
