package co.com.pragma.sqs.sender.senders;

import co.com.pragma.consecuencias.ActualizarEstadoSolicitudMensaje;
import co.com.pragma.errores.ErrorSQS;
import co.com.pragma.model.estado.Estado;
import co.com.pragma.model.estado.gateways.EstadoRepository;
import co.com.pragma.model.mensaje.gateways.MensajeSQSGateway;
import co.com.pragma.model.solicitud.Solicitud;
import co.com.pragma.model.tipoprestamo.TipoPrestamo;
import co.com.pragma.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.pragma.sqs.sender.mapper.ActualizarEstadoSolicitudMensajeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActualizarEstadoSolicitudSender implements MensajeSQSGateway {

    private final EstadoRepository estadoRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ActualizarEstadoSolicitudMensajeMapper mapper;
    private final SQSSender sqsSender;

    @Override
    public Mono<Solicitud> enviarSolicitudActualizada(Solicitud modelo) {
        return Mono.fromCallable(() -> mapper.toMessage(modelo))
                .flatMap(this::transformarEstadoYTipoPrestamo)
                .flatMap(sqsSender::serializar)
                .flatMap(sqsSender::send)
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
        return null;
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
}
