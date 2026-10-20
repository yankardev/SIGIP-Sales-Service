package pe.edu.cibertec.sigip.salesservice.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ServicioNoDisponibleException;

/**
 * Respuesta controlada cuando falla la llamada al catálogo.
 * No inventa precios: si el catálogo no está disponible, el pedido no se registra (503).
 */
@Component
public class CatalogServiceFallbackFactory implements FallbackFactory<CatalogServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceFallbackFactory.class);

    @Override
    public CatalogServiceClient create(Throwable causa) {
        return id -> {
            if (causa instanceof FeignException.NotFound) {
                throw new RecursoNoEncontradoException("El servicio de impresión " + id + " no existe");
            }
            log.warn("catalog-service no disponible al consultar el servicio {}: {}", id, causa.toString());
            throw new ServicioNoDisponibleException("El catálogo no está disponible en este momento, intente nuevamente");
        };
    }
}
