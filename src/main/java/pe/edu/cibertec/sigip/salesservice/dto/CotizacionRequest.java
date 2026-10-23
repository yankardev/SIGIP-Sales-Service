package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotizacionRequest(
        @NotNull Integer idcliente,
        @NotNull Integer idservicio,
        @NotNull @Min(1) @Max(100000) Integer cantidad,
        @DecimalMin(value = "0.01", inclusive = true) BigDecimal precioUnitarioAcordado,
        @Size(max = 1000) String especificaciones,
        LocalDate vigenciaHasta
) {}
