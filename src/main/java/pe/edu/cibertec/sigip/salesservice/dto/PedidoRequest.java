package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoRequest(
        Integer idcliente,

        @NotNull(message = "El servicio es obligatorio")
        Integer idservicio,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 100000, message = "La cantidad máxima es 100000")
        Integer cantidad,

        @Size(max = 1000, message = "Las especificaciones no pueden superar 1000 caracteres")
        String especificaciones,

        LocalDateTime fechaEntrega,

        /** Campo legacy. El backend ya no confía en este valor cuando llegan opcionesSeleccionadas. */
        @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precioUnitarioAcordado,

        /** Campo legacy para compatibilidad; el detalle nuevo se genera en backend. */
        @Size(max = 1000, message = "El detalle del precio no puede superar 1000 caracteres")
        String detallePrecio,

        /** IDs de las opciones elegidas (una por grupo) para servicios personalizables. */
        List<Integer> opcionesSeleccionadas
) {
    /** Contrato inmediatamente anterior. */
    public PedidoRequest(Integer idcliente, Integer idservicio, Integer cantidad, String especificaciones,
                         LocalDateTime fechaEntrega, BigDecimal precioUnitarioAcordado, String detallePrecio) {
        this(idcliente, idservicio, cantidad, especificaciones, fechaEntrega, precioUnitarioAcordado, detallePrecio, null);
    }

    /** Contrato anterior usado por el frontend previo. */
    public PedidoRequest(Integer idcliente, Integer idservicio, Integer cantidad, String especificaciones, LocalDateTime fechaEntrega) {
        this(idcliente, idservicio, cantidad, especificaciones, fechaEntrega, null, null, null);
    }

    /** Constructor compatible con pruebas históricas. */
    public PedidoRequest(Integer idservicio, Integer cantidad, String especificaciones) {
        this(null, idservicio, cantidad, especificaciones, null, null, null, null);
    }
}
