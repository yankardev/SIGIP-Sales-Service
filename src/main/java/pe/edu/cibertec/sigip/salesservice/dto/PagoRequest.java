package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.*;
import pe.edu.cibertec.sigip.salesservice.model.MetodoPago;

import java.math.BigDecimal;

public record PagoRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodo,

        @NotBlank(message = "El número de operación es obligatorio")
        @Size(max = 50, message = "El número de operación no puede superar 50 caracteres")
        String numeroOperacion
) {
}
