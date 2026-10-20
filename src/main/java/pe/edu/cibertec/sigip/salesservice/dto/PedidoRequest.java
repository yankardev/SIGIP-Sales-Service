package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PedidoRequest(
        @NotNull(message = "El servicio es obligatorio")
        Integer idservicio,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 100000, message = "La cantidad máxima es 100000")
        Integer cantidad,

        @Size(max = 1000, message = "Las especificaciones no pueden superar 1000 caracteres")
        String especificaciones
) {
}
