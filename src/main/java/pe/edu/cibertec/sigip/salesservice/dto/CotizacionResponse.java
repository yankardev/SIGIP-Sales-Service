package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CotizacionResponse(
        Integer idcotizacion,
        String cliente,
        Integer idservicio,
        String nombreServicio,
        Integer cantidad,
        String especificaciones,
        BigDecimal precioUnitario,
        BigDecimal total,
        EstadoCotizacion estado,
        String observacion,
        String atendidoPor,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaCotizacion,
        LocalDateTime fechaDecision,
        Integer idpedido
) {
    public static CotizacionResponse from(Cotizacion c) {
        return new CotizacionResponse(c.getIdcotizacion(), c.getCliente(), c.getIdservicio(), c.getNombreServicio(),
                c.getCantidad(), c.getEspecificaciones(), c.getPrecioUnitario(), c.getTotal(), c.getEstado(),
                c.getObservacion(), c.getAtendidoPor(), c.getFechaSolicitud(), c.getFechaCotizacion(),
                c.getFechaDecision(), c.getIdpedido());
    }
}
