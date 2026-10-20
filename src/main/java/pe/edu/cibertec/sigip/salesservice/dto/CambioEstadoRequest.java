package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;

public record CambioEstadoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoPedido estado,

        @Size(max = 500, message = "La observación no puede superar 500 caracteres")
        String observacion
) {
}
