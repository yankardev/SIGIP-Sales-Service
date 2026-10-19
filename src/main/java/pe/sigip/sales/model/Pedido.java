package pe.sigip.sales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long clienteId;
    @NotBlank @Column(nullable = false, length = 180) private String descripcion;
    @NotNull @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(nullable = false, length = 30) private String estado = "PENDIENTE_PAGO";
    @Column(nullable = false) private LocalDateTime fechaCreacion = LocalDateTime.now();
    public Long getId(){return id;} public Long getClienteId(){return clienteId;} public String getDescripcion(){return descripcion;} public BigDecimal getTotal(){return total;} public String getEstado(){return estado;} public LocalDateTime getFechaCreacion(){return fechaCreacion;}
    public void setId(Long v){id=v;} public void setClienteId(Long v){clienteId=v;} public void setDescripcion(String v){descripcion=v;} public void setTotal(BigDecimal v){total=v;} public void setEstado(String v){estado=v;} public void setFechaCreacion(LocalDateTime v){fechaCreacion=v;}
}