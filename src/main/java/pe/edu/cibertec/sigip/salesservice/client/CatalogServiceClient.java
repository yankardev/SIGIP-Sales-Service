package pe.edu.cibertec.sigip.salesservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente declarativo (OpenFeign) hacia catalog-service.
 * "catalog-service" se resuelve con Eureka y el balanceador de carga: si hay varias instancias,
 * las peticiones se reparten entre ellas. Si el catálogo no responde, actúa el circuit breaker (fallback).
 */
@FeignClient(name = "catalog-service", fallbackFactory = CatalogServiceFallbackFactory.class)
public interface CatalogServiceClient {

    @GetMapping("/api/v1/servicios/{id}")
    ServicioCatalogoResponse obtenerServicio(@PathVariable("id") Integer id);
}
