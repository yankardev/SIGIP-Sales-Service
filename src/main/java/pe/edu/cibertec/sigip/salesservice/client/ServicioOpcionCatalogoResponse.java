package pe.edu.cibertec.sigip.salesservice.client;

import java.math.BigDecimal;

/** Opción vigente de un servicio personalizable expuesta por catalog-service. */
public record ServicioOpcionCatalogoResponse(
        Integer idopcion,
        Integer idservicio,
        String grupo,
        String nombre,
        BigDecimal incremento,
        Integer orden
) {
}
