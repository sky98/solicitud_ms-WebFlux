package co.com.pragma.usecase.obtenersolicitudesporestado;

import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.solicitud.DetallesSolicitudes;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ObtenerSolicitudesPorEstadoUseCase {

    private final SolicitudRepository solicitudRepository;
    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public Flux<DetallesSolicitudes> ejecutar(Integer estadoId, Integer limit, Integer offset){
        return solicitudRepository.obtenerSolicitudesPorEstado(estadoId, limit, offset)
                .map(this::toDetallesSolicitudes);
    }

    private Mono<DetallesSolicitudes> complementarSolicitud(Solicitud solicitud){
        Mono<String> monoEstado = estadoRepository.obtenerNombreEstadoPorId(solicitud.getEstadoId());
        Mono<TipoPrestamo> monoTipoPrestamo = tipoPrestamoRepository.obtenerPorId(solicitud.getTipoPrestamoId());
        return null;
    }

    private DetallesSolicitudes toDetallesSolicitudes(Solicitud solicitud){
        if(solicitud == null)
            return null;

        DetallesSolicitudes.DetallesSolicitudesBuilder detallesSolicitudes = DetallesSolicitudes.builder();

        detallesSolicitudes.monto(solicitud.getMonto());
        detallesSolicitudes.plazo(solicitud.getPlazo());
        detallesSolicitudes.documentoId(solicitud.getDocumentoId());

        return detallesSolicitudes.build();
    }

}
