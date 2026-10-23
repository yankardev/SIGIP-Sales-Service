package pe.edu.cibertec.sigip.salesservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.cibertec.sigip.salesservice.model.Cliente;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCase(String tipoDocumento, String numeroDocumento);
    boolean existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCaseAndIdclienteNot(String tipoDocumento, String numeroDocumento, Integer idcliente);
    List<Cliente> findAllByOrderByRazonSocialAsc();
    List<Cliente> findByActivoTrueOrderByRazonSocialAsc();
}
