package co.com.pragma.model.solicitud;

import java.util.stream.Stream;

public enum EstadoSolicitud {
    PENDIENTE_REVISION(1L),
    RECHAZAD0(2L),
    REVISION_MANUAL(3L),
    APROBADO(4L);

    private final Long id;

    EstadoSolicitud(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static Long getIdByNombre(String nombre) {
        return Stream.of(EstadoSolicitud.values())
                .filter(estado -> estado.name().equals(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de solicitud no válido: " + nombre))
                .getId();
    }
}
