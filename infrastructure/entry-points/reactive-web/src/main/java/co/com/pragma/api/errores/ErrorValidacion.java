package co.com.pragma.api.errores;

import co.com.pragma.errores.ApplicationError;

import java.util.Set;

public class ErrorValidacion extends RuntimeException implements ApplicationError {

    private Set<String> campos;

    public ErrorValidacion(String mensaje, Set<String> campos) {
        super(mensaje);
        this.campos = campos;
    }

    @Override
    public Set<String> getCampos() {
        return campos;
    }

}
