package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CotizacionSolicitudRequest(
        @NotNull Integer idservicio,
        @NotNull @Min(1) @Max(100000) Integer cantidad,
        @Size(max = 1000) String especificaciones,
        @Size(max = 50) String cliente
) {
}
