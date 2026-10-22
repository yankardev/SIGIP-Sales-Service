package pe.edu.cibertec.sigip.salesservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizaciones", indexes = {
        @Index(name = "idx_cotizacion_cliente", columnList = "cliente"),
        @Index(name = "idx_cotizacion_estado", columnList = "estado")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcotizacion;

    @Column(nullable = false, length = 50)
    private String cliente;

    @Column(nullable = false)
    private Integer idservicio;

    @Column(nullable = false, length = 150)
    private String nombreServicio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 1000)
    private String especificaciones;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado;

    @Column(length = 500)
    private String observacion;

    @Column(length = 50)
    private String atendidoPor;

    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaCotizacion;
    private LocalDateTime fechaDecision;

    /** Pedido generado al aceptar la cotización. */
    private Integer idpedido;

    @Version
    private Long version;
}
