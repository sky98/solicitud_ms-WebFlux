package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

//TODO realizar pruebas por que parece que no esta reaizando las validaciones de los bigDecimal
@Builder
public record CrearSolicitudRequestDTO(
        @NotBlank(message = "El campo :documentoId es obligatorio")
        @Digits(integer = 12, fraction = 0, message = "El campo :documentoId debe ser un número entero sin decimales y con un máximo de 12 dígitos")
        @Size(min = 7, max = 12, message = "El campo :documentoId debe tener entre 7 y 12 caracteres")
        String documentoId,
        @NotNull(message = "El campo :monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El campo :monto debe ser mayor a 0")
        BigDecimal monto,
        @NotNull(message = "El campo :plazo es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El campo :plazo debe ser mayor a 0")
        BigDecimal plazo,
        @NotNull(message = "El campo :tipoPrestamoId es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El campo :tipoPrestamoId debe ser mayor a 0")
        BigDecimal tipoPrestamoId
) {
}
