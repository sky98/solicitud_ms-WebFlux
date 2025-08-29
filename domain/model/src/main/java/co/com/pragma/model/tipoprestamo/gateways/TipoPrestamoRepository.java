package co.com.pragma.model.tipoprestamo.gateways;

import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TipoPrestamoRepository {
    Mono<TipoPrestamo> obtenerPorId(Long tipoPrestamoId);
    Mono<Boolean> existeMontoEnRango(Long tipoPrestamoId, BigDecimal monto);
}
