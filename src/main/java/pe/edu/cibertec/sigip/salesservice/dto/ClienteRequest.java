package pe.edu.cibertec.sigip.salesservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 10) String tipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @NotBlank @Size(max = 180) String razonSocial,
        @Size(max = 120) String contacto,
        @Size(max = 30) String telefono,
        @Email @Size(max = 150) String email,
        @Size(max = 250) String direccion,
        Boolean activo
) {}
