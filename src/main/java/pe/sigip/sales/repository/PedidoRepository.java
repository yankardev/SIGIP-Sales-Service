package pe.sigip.sales.repository;
import pe.sigip.sales.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PedidoRepository extends JpaRepository<Pedido,Long>{}