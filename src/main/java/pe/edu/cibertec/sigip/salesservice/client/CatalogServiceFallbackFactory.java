package pe.edu.cibertec.sigip.salesservice.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ServicioNoDisponibleException;

import java.util.List;

/** Respuesta controlada cuando catalog-service no está disponible. */
@Component
public class CatalogServiceFallbackFactory implements FallbackFactory<CatalogServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceFallbackFactory.class);

    @Override
    public CatalogServiceClient create(Throwable causa) {
        return new CatalogServiceClient() {
            @Override
            public ServicioCatalogoResponse obtenerServicio(Integer id) {
                if (causa instanceof FeignException.NotFound) {
                    throw new RecursoNoEncontradoException("El servicio de impresión " + id + " no existe");
                }
                log.warn("catalog-service no disponible al consultar el servicio {}: {}", id, causa.toString());
                throw new ServicioNoDisponibleException("El catálogo no está disponible en este momento, intente nuevamente");
            }

            @Override
            public List<ServicioOpcionCatalogoResponse> obtenerOpciones(Integer id) {
                if (causa instanceof FeignException.NotFound) {
                    throw new RecursoNoEncontradoException("El servicio de impresión " + id + " no existe");
                }
                log.warn("catalog-service no disponible al consultar opciones del servicio {}: {}", id, causa.toString());
                throw new ServicioNoDisponibleException("No se pudieron consultar las opciones del servicio, intente nuevamente");
            }
        };
    }
}
