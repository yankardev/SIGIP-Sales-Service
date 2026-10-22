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
        String especificaciones,

        /**
         * Usuario CLIENTE al que pertenece el pedido. Solo lo utiliza el personal interno;
         * para un CLIENTE autenticado el backend ignora este valor y usa el usuario del JWT.
         */
        @Size(max = 50, message = "El cliente no puede superar 50 caracteres")
        String cliente
) {
}
