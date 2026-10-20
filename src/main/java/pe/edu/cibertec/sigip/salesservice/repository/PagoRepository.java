package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPago;
import pe.edu.cibertec.sigip.salesservice.model.Pago;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByPedidoIdpedidoOrderByFechaRegistroDesc(Integer idpedido);

    boolean existsByPedidoIdpedidoAndEstado(Integer idpedido, EstadoPago estado);

    boolean existsByPedidoIdpedido(Integer idpedido);
}
