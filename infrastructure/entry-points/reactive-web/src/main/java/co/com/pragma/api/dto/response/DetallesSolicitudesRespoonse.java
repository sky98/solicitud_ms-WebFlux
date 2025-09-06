package co.com.pragma.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DetallesSolicitudesRespoonse(
        Long documentoId,
        String correoElectronico,
        BigDecimal salarioBase,
        String nombresUsuario,
        String apellidosUsuario,
        BigDecimal monto,
        Long plazo,
        String estadoSolicitud,
        String tipoPrestamo,
        BigDecimal tasaInteresPrestamo,
        BigDecimal deudaTotalMensualSolicitudesAprobadas
) {
}
