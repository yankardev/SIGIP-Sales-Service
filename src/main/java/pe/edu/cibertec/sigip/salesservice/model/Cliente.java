package pe.edu.cibertec.sigip.salesservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes", uniqueConstraints = @UniqueConstraint(name = "uk_cliente_documento", columnNames = {"tipo_documento", "numero_documento"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcliente;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 180)
    private String razonSocial;

    @Column(length = 120)
    private String contacto;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String email;

    @Column(length = 250)
    private String direccion;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaActualizacion;
}
