package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    /** Compatibilidad con pedidos antiguos donde cliente era el usuario final. */
    List<Pedido> findByClienteOrderByFechaPedidoDesc(String cliente);

    List<Pedido> findByIdclienteOrderByFechaPedidoDesc(Integer idcliente);
    List<Pedido> findByEstadoOrderByFechaPedidoDesc(EstadoPedido estado);
    List<Pedido> findAllByOrderByFechaPedidoDesc();

    @Query("select p.estado as estado, count(p) as cantidad from Pedido p group by p.estado")
    List<ConteoPorEstado> contarPorEstado();

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.estado in :estados")
    BigDecimal sumarTotalPorEstados(Collection<EstadoPedido> estados);

    interface ConteoPorEstado {
        EstadoPedido getEstado();
        Long getCantidad();
    }
}
