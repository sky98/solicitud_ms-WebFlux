package co.com.pragma.model.usuario.gateways;

import co.com.pragma.model.usuario.Usuario;
import reactor.core.publisher.Mono;

public interface UsuarioResConsumerGateway {
    Mono<Boolean> validarUsuarioPorDocumentoId(Long documentoId);
    Mono<Usuario> obtenerUsuarioPorDocumentoId(Long documentoId);
}
