package pe.edu.cibertec.sigip.salesservice.dto;

import java.math.BigDecimal;
import java.util.Map;

/** Resumen que consume report-service. */
public record ResumenVentasResponse(
        long totalPedidos,
        Map<String, Long> pedidosPorEstado,
        BigDecimal ventasConfirmadas
) {
}
