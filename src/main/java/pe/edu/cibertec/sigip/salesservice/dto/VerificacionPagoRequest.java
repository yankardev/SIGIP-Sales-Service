package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificacionPagoRequest(
        @NotNull(message = "Debe indicar si el pago se aprueba")
        Boolean aprobado,

        @Size(max = 300, message = "La observación no puede superar 300 caracteres")
        String observacion
) {
}
