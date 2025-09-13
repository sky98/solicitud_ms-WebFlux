package co.com.pragma.sqs.sender.senders;

import co.com.pragma.sqs.sender.mapper.MapperMensajesUtils;
import co.com.pragma.sqs.sender.mensajes.ActualizarEstadoSolicitudMensaje;
import co.com.pragma.sqs.sender.mensajes.CalcularCapacidadEndeudamientoMensaje;
import co.com.pragma.errores.ErrorSQS;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.solicitud.gateways.SolicitudRepository;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.model.usuario.Usuario;
import co.com.pragma.model.usuario.gateways.UsuarioResConsumerGateway;
import co.com.pragma.sqs.sender.mapper.SolicitudMensajeMapper;
import co.com.pragma.sqs.sender.mensajes.SolicitudLite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenderSolicitud implements MensajeSQSGateway {

    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioResConsumerGateway usuarioResConsumerGateway;
    private final SolicitudMensajeMapper solicitudMensajeMapper;
    private final SQSSender sqsSender;
    private final MapperMensajesUtils mapperMensajesUtils;

    private static String QUEUE_ACTUALIZAR_ESTADO_SOLICITUD = "/solicitudes";
    private static String QUEUE_CAPACIDAD_ENDEUDAMIENTO = "/capacidad-endeudamiento";

    @Override
    public Mono<Solicitud> enviarSolicitudActualizada(Solicitud modelo) {
        return Mono.fromCallable(() -> solicitudMensajeMapper.toMessage(modelo))
                .flatMap(this::transformarEstadoYTipoPrestamo)
                .flatMap(mapperMensajesUtils::serializar)
                .flatMap(msj -> sqsSender.send(msj,QUEUE_ACTUALIZAR_ESTADO_SOLICITUD))
                .doOnSuccess(token -> log.info("Mensaje enviado con exito : {}", token))
                .onErrorResume(e -> {
                    log.error("Se ha generado un error al enviar mensaje a SQS : {}", e.getMessage());
                    return Mono.error(
                            new ErrorSQS("Se ha generado un error al enviar mensaje a SQS : " + e.getMessage(), Set.of(e.getMessage()))
                    );
                })
                .map(resp -> modelo);
    }

    @Override
    public Mono<Solicitud> calcularCapacidadEndeudamiento(Solicitud solicitud) {
        return obtenerDatosParaCalculoDeCapacidadDeEndeudamiento(solicitud)
                .flatMap(mapperMensajesUtils::serializar)
                .flatMap(msj -> sqsSender.send(msj, QUEUE_CAPACIDAD_ENDEUDAMIENTO))
                .doOnSuccess(token -> log.info("Mensaje enviado a la cola para calculo de endeudamiento con exito : {}", token))
                .onErrorResume(e ->{
                    log.error("Se ha generado un error al enviar mensaje a SQS : {}", e.getMessage());
                    return Mono.error(
                            new ErrorSQS("Se ha generado un error al enviar mensaje a SQS : " + e.getMessage(), Set.of(e.getMessage()))
                    );
                }).thenReturn(solicitud);
    }

    private Mono<ActualizarEstadoSolicitudMensaje> transformarEstadoYTipoPrestamo(ActualizarEstadoSolicitudMensaje msj){
        Mono<Estado> monoEstado = estadoRepository.obtenerPorId(Long.valueOf(msj.getEstado()));
        Mono<TipoPrestamo> monoTipoPrestamo = tipoPrestamoRepository.obtenerPorId(Long.valueOf(msj.getTipoPrestamo()));
        return Mono.zip(monoEstado, monoTipoPrestamo)
                .map(tupla -> {
                    msj.setTipoPrestamo(tupla.getT2().getNombre());
                    msj.setEstado(tupla.getT1().getNombre());
                    return msj;
                });
    }

    private Mono<CalcularCapacidadEndeudamientoMensaje> obtenerDatosParaCalculoDeCapacidadDeEndeudamiento(Solicitud solicitud){
        Mono<TipoPrestamo> tipoPrestamoMono = tipoPrestamoRepository.obtenerPorId(solicitud.getTipoPrestamoId());
        Mono<List<Solicitud>> listSolicitudesMono = solicitudRepository.obtenerSolicitudesPorDocumentoIdAprobadas(solicitud.getDocumentoId())
                .collectList();
        Mono<Usuario> usuarioMono = usuarioResConsumerGateway.obtenerUsuarioPorDocumentoId(solicitud.getDocumentoId());
        Mono<List<SolicitudLite>> solicitudesLiteListMono = listSolicitudesMono.flatMap(solicitudes -> {
            List<Long> tipoPrestamosIdUnicos = solicitudes.stream()
                    .map(Solicitud::getTipoPrestamoId)
                    .distinct()
                    .toList();
            Mono<Map<Long, BigDecimal>> tasasMapMono = Flux.fromIterable(tipoPrestamosIdUnicos)
                    .flatMap(tipoPrestamoRepository::obtenerPorId)
                    .collect(Collectors.toMap(TipoPrestamo::getTipoPrestamoId, TipoPrestamo::getTasaInteres));

            return tasasMapMono.map(tasasMap -> solicitudes.stream()
                    .map(sol -> SolicitudLite.builder()
                            .monto(sol.getMonto())
                            .plazo(sol.getPlazo())
                            .tasaInteres(tasasMap.getOrDefault(sol.getTipoPrestamoId(), BigDecimal.ZERO))
                            .build()
                    ).collect(Collectors.toList())
            );
        });

        return Mono.zip(
                tipoPrestamoMono,
                solicitudesLiteListMono,
                usuarioMono,
                Mono.just(solicitud)
        ).map(tuple4 -> construirMensajeCalcularCapacidadEndeudamiento(tuple4.getT4(), tuple4.getT3(), tuple4.getT1(), tuple4.getT2()));
    }

    private CalcularCapacidadEndeudamientoMensaje construirMensajeCalcularCapacidadEndeudamiento(Solicitud solicitud, Usuario usuario, TipoPrestamo tipoPrestamo, List<SolicitudLite> solicitudesLite){
        return CalcularCapacidadEndeudamientoMensaje.builder()
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .solicitudId(solicitud.getSolicitudId())
                .monto(solicitud.getMonto())
                .plazo(solicitud.getPlazo())
                .salarioBase(usuario.getSalarioBase())
                .tasaInteresMensual(tipoPrestamo.getTasaInteres())
                .tipoPrestamo(tipoPrestamo.getNombre())
                .documentoId(solicitud.getDocumentoId())
                .solicitudes(solicitudesLite)
                .build();
    }
}
