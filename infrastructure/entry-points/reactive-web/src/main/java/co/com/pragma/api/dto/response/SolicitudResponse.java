package co.com.pragma.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SolicitudResponse(
        Long solicitudId,
        BigDecimal monto,
        Long plazo,
        Long estadoId,
        Long tipoPrestamoId,
        Long documentoId
) {
}
