package co.com.pragma.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class Paginacion<T> {
    private long numeroPagina;
    private long tamanoPagina;
    private long totalElementos;
    private long totalPaginas;
    private List<T> items;
}
