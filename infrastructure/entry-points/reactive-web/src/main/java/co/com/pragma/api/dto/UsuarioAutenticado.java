package co.com.pragma.api.dto;

import java.util.List;

public record UsuarioAutenticado(
        String uid,
        Integer rolId,
        List<String> roles
) {
}
