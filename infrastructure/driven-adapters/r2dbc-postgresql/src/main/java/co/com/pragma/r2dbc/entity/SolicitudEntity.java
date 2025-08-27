package co.com.pragma.r2dbc.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("solicitud")
public class SolicitudEntity {

    @Id
    @Column("solicitud_id")
    private Long solicitudId;
    private BigDecimal monto;
    private Long plazo;
    @Column("estado_id")
    private Long estadoId;
    @Column("tipo_prestamo_id")
    private Long tipoPrestamoId;
    @Column("usuario_documento_id")
    private Long usuarioDocumentoId;
}
