package pe.edu.cibertec.sigip.salesservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cotizaciones", indexes = {
        @Index(name = "idx_cotizacion_cliente", columnList = "idcliente"),
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

    @Column(nullable = false)
    private Integer idcliente;

    @Column(nullable = false, length = 180)
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

    @Column(length = 1000)
    private String especificaciones;

    private LocalDate vigenciaHasta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado;

    @Column(nullable = false, length = 50)
    private String creadoPor;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaActualizacion;
}
