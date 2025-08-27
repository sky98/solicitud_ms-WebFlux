package co.com.pragma.r2dbc.errores;

import co.com.pragma.errores.ApplicationError;

import java.util.Set;

public class ErrorPersistencia extends RuntimeException implements ApplicationError {

    private Set<String> campos;

    public ErrorPersistencia(String mensaje, Set<String> campos) {
        super(mensaje);
        this.campos = campos;
    }

    @Override
    public Set<String> getCampos() {
        return campos;
    }
}
