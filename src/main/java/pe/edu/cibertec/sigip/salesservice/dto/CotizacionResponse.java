package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CotizacionResponse(
        Integer idcotizacion,
        Integer idcliente,
        String cliente,
        Integer idservicio,
        String nombreServicio,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal total,
        String especificaciones,
        LocalDate vigenciaHasta,
        EstadoCotizacion estado,
        String creadoPor,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion
) {
    public static CotizacionResponse from(Cotizacion c) {
        return new CotizacionResponse(c.getIdcotizacion(), c.getIdcliente(), c.getCliente(), c.getIdservicio(),
                c.getNombreServicio(), c.getPrecioUnitario(), c.getCantidad(), c.getTotal(), c.getEspecificaciones(),
                c.getVigenciaHasta(), c.getEstado(), c.getCreadoPor(), c.getFechaRegistro(), c.getFechaActualizacion());
    }
}
