package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByClienteOrderByFechaPedidoDesc(String cliente);

    List<Pedido> findByEstadoOrderByFechaPedidoDesc(EstadoPedido estado);

    List<Pedido> findAllByOrderByFechaPedidoDesc();

    /** Conteo agrupado en la BD (una consulta) en lugar de traer todos los pedidos a memoria. */
    @Query("select p.estado as estado, count(p) as cantidad from Pedido p group by p.estado")
    List<ConteoPorEstado> contarPorEstado();

    @Query("select coalesce(sum(p.total), 0) from Pedido p where p.estado in :estados")
    BigDecimal sumarTotalPorEstados(Collection<EstadoPedido> estados);

    interface ConteoPorEstado {
        EstadoPedido getEstado();

        Long getCantidad();
    }
}
