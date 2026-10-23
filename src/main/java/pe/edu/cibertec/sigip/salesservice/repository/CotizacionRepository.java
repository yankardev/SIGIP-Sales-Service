package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    List<Cotizacion> findAllByOrderByFechaRegistroDesc();
    List<Cotizacion> findByEstadoOrderByFechaRegistroDesc(EstadoCotizacion estado);
    List<Cotizacion> findByIdclienteOrderByFechaRegistroDesc(Integer idcliente);
}
