package co.com.pragma.errores;

import java.util.Set;

public class ErrorDominio extends RuntimeException implements ApplicationError{

  private Set<String> campos;

  public ErrorDominio(String mensaje, Set<String> campos) {
    super(mensaje);
    this.campos = campos;
  }

  @Override
  public Set<String> getCampos() {
    return campos;
  }
}
