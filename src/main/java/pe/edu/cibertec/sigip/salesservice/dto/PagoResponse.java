package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.EstadoPago;
import pe.edu.cibertec.sigip.salesservice.model.MetodoPago;
import pe.edu.cibertec.sigip.salesservice.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Integer idpago,
        Integer idpedido,
        BigDecimal monto,
        MetodoPago metodo,
        String numeroOperacion,
        EstadoPago estado,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaVerificacion,
        String verificadoPor,
        String observacion
) {
    public static PagoResponse from(Pago p) {
        return new PagoResponse(p.getIdpago(), p.getPedido().getIdpedido(), p.getMonto(), p.getMetodo(),
                p.getNumeroOperacion(), p.getEstado(), p.getFechaRegistro(), p.getFechaVerificacion(),
                p.getVerificadoPor(), p.getObservacion());
    }
}
