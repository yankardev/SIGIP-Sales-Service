package pe.edu.cibertec.sigip.salesservice.dto;

import pe.edu.cibertec.sigip.salesservice.model.Cliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Integer idcliente,
        String tipoDocumento,
        String numeroDocumento,
        String razonSocial,
        String contacto,
        String telefono,
        String email,
        String direccion,
        boolean activo,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion
) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getIdcliente(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getRazonSocial(),
                c.getContacto(), c.getTelefono(), c.getEmail(), c.getDireccion(), c.isActivo(),
                c.getFechaRegistro(), c.getFechaActualizacion());
    }
}
