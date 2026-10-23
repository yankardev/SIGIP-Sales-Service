package pe.edu.cibertec.sigip.salesservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Cliente declarativo (OpenFeign) hacia catalog-service.
 * "catalog-service" se resuelve con Eureka y el balanceador de carga.
 */
@FeignClient(name = "catalog-service", fallbackFactory = CatalogServiceFallbackFactory.class)
public interface CatalogServiceClient {

    @GetMapping("/api/v1/servicios/{id}")
    ServicioCatalogoResponse obtenerServicio(@PathVariable("id") Integer id);

    @GetMapping("/api/v1/servicios/{id}/opciones")
    List<ServicioOpcionCatalogoResponse> obtenerOpciones(@PathVariable("id") Integer id);
}
