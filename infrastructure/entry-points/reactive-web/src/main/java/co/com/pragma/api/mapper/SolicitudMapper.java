package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.ActualizarEstadoSolicitudRequestDTO;
import co.com.pragma.api.dto.request.CrearSolicitudRequestDTO;
import co.com.pragma.api.dto.response.SolicitudResponse;
import co.com.pragma.model.solicitud.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {
    @Mapping(target = "estadoId", constant = "1L")
    @Mapping(target = "solicitudId", ignore = true)
    Solicitud toModel(CrearSolicitudRequestDTO crearSolicitudRequestDTO);
    SolicitudResponse toResponse(Solicitud solicitud);

    Solicitud toModel(ActualizarEstadoSolicitudRequestDTO request);
}
