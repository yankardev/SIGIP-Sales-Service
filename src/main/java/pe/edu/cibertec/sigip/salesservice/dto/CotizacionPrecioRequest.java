package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CotizacionPrecioRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal precioUnitario,
        @Size(max = 500) String observacion
) {
}
