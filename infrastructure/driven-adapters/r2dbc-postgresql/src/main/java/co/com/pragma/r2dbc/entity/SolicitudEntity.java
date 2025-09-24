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
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("solicitudes")
public class SolicitudEntity {
    @Id
    @Column("solicitud_id")
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    @Column("estado_id")
    private Long estadoId;
    @Column("tipo_prestamo_id")
    private String tipoPrestamoId;
    @Column("documento_id")
    private Long documentoId;
    @Column("fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
}
