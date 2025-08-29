package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("tipo_prestamos")
public class TipoPrestamoEntity {
    @Id
    @Column("tipo_prestamo_id")
    private Long tipoPrestamoId;
    private String nombre;
    @Column("monto_minimo")
    private BigDecimal montoMinimo;
    @Column("monto_maximo")
    private BigDecimal montoMaximo;
    @Column("tasa_interes")
    private BigDecimal tasaInteres;
    @Column("validacion_automatica")
    private String validacionAutomatica;
}
