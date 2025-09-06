package co.com.pragma.errores;

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
