package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record PedidoResponse(
        Integer idpedido,
        String cliente,
        Integer idservicio,
        String nombreServicio,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal total,
        EstadoPedido estado,
        Set<EstadoPedido> siguientesEstados,
        String especificaciones,
        String observacion,
        LocalDateTime fechaPedido,
        LocalDateTime fechaActualizacion
) {
    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(p.getIdpedido(), p.getCliente(), p.getIdservicio(), p.getNombreServicio(),
                p.getPrecioUnitario(), p.getCantidad(), p.getTotal(), p.getEstado(), p.getEstado().siguientes(),
                p.getEspecificaciones(), p.getObservacion(), p.getFechaPedido(), p.getFechaActualizacion());
    }
}
