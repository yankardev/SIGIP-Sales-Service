package pe.edu.cibertec.sigip.salesservice.client;

import java.math.BigDecimal;

public record ServicioCatalogoResponse(
        Integer idservicio,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        Boolean requiereCotizacion,
        Boolean activo,
        Integer idcategoria,
        String categoria,
        Boolean personalizable,
        String imagenUrl
) {
    /** Contrato anterior utilizado por pruebas y clientes existentes. */
    public ServicioCatalogoResponse(Integer idservicio, String nombre, String descripcion, BigDecimal precioBase,
                                     Boolean requiereCotizacion, Boolean activo, Integer idcategoria, String categoria) {
        this(idservicio, nombre, descripcion, precioBase, requiereCotizacion, activo, idcategoria, categoria, false, null);
    }
}
