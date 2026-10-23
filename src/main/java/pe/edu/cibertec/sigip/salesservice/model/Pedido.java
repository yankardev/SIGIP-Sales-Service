package pe.edu.cibertec.sigip.salesservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pedido interno de la empresa asociado a un cliente comercial registrado. */
@Entity
@Table(name = "pedidos", indexes = {
        @Index(name = "idx_pedido_cliente", columnList = "cliente"),
        @Index(name = "idx_pedido_idcliente", columnList = "idcliente"),
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

    private Integer idcliente;

    @Column(nullable = false, length = 180)
    private String cliente;

    @Column(length = 50)
    private String registradoPor;

    private Integer idcotizacion;

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

    @Column(length = 1000)
    private String especificaciones;

    /** Desglose usado para saber exactamente qué opciones originaron el precio cobrado. */
    @Column(name = "detalle_precio", length = 1000)
    private String detallePrecio;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fechaPedido;

    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaActualizacion;

    @Version
    private Long version;
}
