package co.com.pragma.fabricas;

import co.com.pragma.model.usuario.Usuario;

import java.math.BigDecimal;

public class UsuarioFabrica {

    private Usuario.UsuarioBuilder builder;

    public UsuarioFabrica(){
        this.builder = Usuario.builder()
                .documentoId(1L)
                .nombres("tets nombre")
                .apellidos("test apellidos")
                .correoElectronico("correo@test.com")
                .salarioBase(BigDecimal.TEN);
    }

    public static UsuarioFabrica builder() { return new UsuarioFabrica(); }

    public Usuario.UsuarioBuilder with(){ return this.builder; }

    public Usuario build(){ return this.builder.build(); }

}
