package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record PedidoResponse(
        Integer idpedido,
        Integer idcliente,
        String cliente,
        String registradoPor,
        Integer idcotizacion,
        Integer idservicio,
        String nombreServicio,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal total,
        EstadoPedido estado,
        Set<EstadoPedido> siguientesEstados,
        String especificaciones,
        String detallePrecio,
        String observacion,
        LocalDateTime fechaPedido,
        LocalDateTime fechaEntrega,
        LocalDateTime fechaActualizacion
) {
    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(p.getIdpedido(), p.getIdcliente(), p.getCliente(), p.getRegistradoPor(),
                p.getIdcotizacion(), p.getIdservicio(), p.getNombreServicio(), p.getPrecioUnitario(),
                p.getCantidad(), p.getTotal(), p.getEstado(), p.getEstado().siguientes(), p.getEspecificaciones(),
                p.getDetallePrecio(), p.getObservacion(), p.getFechaPedido(), p.getFechaEntrega(), p.getFechaActualizacion());
    }
}
