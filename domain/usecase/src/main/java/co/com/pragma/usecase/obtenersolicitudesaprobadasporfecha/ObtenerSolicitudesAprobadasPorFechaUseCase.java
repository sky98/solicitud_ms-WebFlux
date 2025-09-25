package co.com.pragma.usecase.obtenersolicitudesaprobadasporfecha;

import co.com.pragma.model.reportediario.ReporteDiario;
import co.com.pragma.model.reportediario.gateways.ReporteSolicitud;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ObtenerSolicitudesAprobadasPorFechaUseCase {

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final UsuarioResConsumerGateway usuarioResConsumerGateway;

    public Mono<List<ReporteDiario>> ejecutar(LocalDateTime fechaInicio, LocalDateTime fechaFin){
        return solicitudRepository.obtenerSolicitudesAprobadaPorFecha(fechaInicio, fechaFin)
                .collectList()
                .flatMap(this::obtenerYProcesarData);
    }

    private Mono<List<ReporteDiario>> obtenerYProcesarData(List<Solicitud> solicitudes){
        List<Long> documentosId = solicitudes.stream()
                .map(Solicitud::getDocumentoId)
                .distinct()
                .toList();
        List<Long> tiposDePrestamoId = solicitudes.stream()
                .map(Solicitud::getTipoPrestamoId)
                .distinct()
                .toList();

        Mono<List<Usuario>> usuariosMono = Flux.fromIterable(documentosId)
                .flatMap(usuarioResConsumerGateway::obtenerUsuarioPorDocumentoId)
                .collectList();
        Mono<List<TipoPrestamo>> tiposDePrestamos = Flux.fromIterable(tiposDePrestamoId)
                .flatMap(tipoPrestamoRepository::obtenerPorId)
                .collectList();

        return Mono.zip( Mono.just(solicitudes), usuariosMono, tiposDePrestamos)
                .map(tuple -> {
                    List<Solicitud> solicitudesList = tuple.getT1();
                    List<Usuario> usuariosList = tuple.getT2();
                    List<TipoPrestamo> tipoPrestamoList = tuple.getT3();

                    Map<Long, Usuario> usuarioMap = usuariosList.stream()
                            .collect(Collectors.toMap(Usuario::getDocumentoId, Function.identity()));
                    Map<Long, TipoPrestamo> tipoPrestamoMap = tipoPrestamoList.stream()
                            .collect(Collectors.toMap(TipoPrestamo::getTipoPrestamoId, Function.identity()));

                    return solicitudesList.stream()
                            .map(solicitud -> {
                                Usuario usuario = usuarioMap.get(solicitud.getDocumentoId());
                                TipoPrestamo tipoPrestamo = tipoPrestamoMap.get(solicitud.getTipoPrestamoId());

                                return ReporteDiario.builder()
                                        .documentoId(solicitud.getDocumentoId())
                                        .correoElectronico(usuario.getCorreoElectronico())
                                        .salarioBase(usuario.getSalarioBase())
                                        .nombresUsuario(usuario.getNombres())
                                        .apellidosUsuario(usuario.getApellidos())
                                        .reporteSolicitudes(
                                                List.of(ReporteSolicitud.builder()
                                                        .solicitudId(solicitud.getSolicitudId())
                                                        .monto(solicitud.getMonto())
                                                        .plazo(solicitud.getPlazo())
                                                        .tipoPrestamo(tipoPrestamo.getNombre())
                                                        .tasaInteres(tipoPrestamo.getTasaInteres())
                                                        .build())
                                        )
                                        .build();

                            })
                            .collect(Collectors.toList());
                });
    }

}
