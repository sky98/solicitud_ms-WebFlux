package co.com.pragma.sqs.sender.mapper;

import co.com.pragma.consecuencias.ActualizarEstadoSolicitudMensaje;
import co.com.pragma.model.solicitud.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class ActualizarEstadoSolicitudMensajeMapper {

    public ActualizarEstadoSolicitudMensaje toMessage(Solicitud solicitud){
        return ActualizarEstadoSolicitudMensaje.builder()
                .solicitudId(solicitud.getSolicitudId())
                .monto(solicitud.getMonto())
                .plazo(solicitud.getPlazo())
                .documentoId(solicitud.getDocumentoId())
                .tipoPrestamo(String.valueOf(solicitud.getTipoPrestamoId()))
                .estado(String.valueOf(solicitud.getEstadoId()))
                .build();
    }

}
