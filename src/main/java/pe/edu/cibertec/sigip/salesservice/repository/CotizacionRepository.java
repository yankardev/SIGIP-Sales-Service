package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {
    List<Cotizacion> findAllByOrderByFechaSolicitudDesc();
    List<Cotizacion> findByClienteOrderByFechaSolicitudDesc(String cliente);
    List<Cotizacion> findByEstadoOrderByFechaSolicitudDesc(EstadoCotizacion estado);
}
