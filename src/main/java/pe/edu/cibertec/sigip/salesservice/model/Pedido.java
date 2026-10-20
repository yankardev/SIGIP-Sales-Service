package pe.edu.cibertec.sigip.salesservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pedido de un cliente. Guarda una copia del nombre y precio del servicio al momento de la compra:
 * si luego cambia el catálogo, el pedido ya acordado no se altera.
 */
@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_pedido_cliente", columnList = "cliente"),
        @Index(name = "idx_pedido_estado", columnList = "estado")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idpedido;

    /** Usuario (nomusuario) dueño del pedido, tomado del token, nunca del JSON. */
    @Column(nullable = false, length = 50)
    private String cliente;

    @Column(nullable = false)
    private Integer idservicio;

    @Column(nullable = false, length = 150)
    private String nombreServicio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    /** Indicaciones del cliente: medidas, colores, enlace al archivo, etc. */
    @Column(length = 1000)
    private String especificaciones;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fechaPedido;

    private LocalDateTime fechaActualizacion;

    /** Bloqueo optimista: evita que dos actualizaciones simultáneas se pisen. */
    @Version
    private Long version;
}
