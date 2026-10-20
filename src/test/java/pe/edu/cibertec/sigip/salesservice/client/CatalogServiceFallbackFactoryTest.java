package pe.edu.cibertec.sigip.salesservice.client;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ServicioNoDisponibleException;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Resiliencia: el fallback del circuit breaker nunca inventa precios. */
class CatalogServiceFallbackFactoryTest {

    private final CatalogServiceFallbackFactory factory = new CatalogServiceFallbackFactory();

    @Test
    void servicioInexistenteDevuelve404() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/servicios/99", Map.of(), null,
                StandardCharsets.UTF_8, null);
        FeignException notFound = new FeignException.NotFound("no existe", request, null, Map.of());

        assertThatThrownBy(() -> factory.create(notFound).obtenerServicio(99))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void catalogoCaidoDevuelve503() {
        assertThatThrownBy(() -> factory.create(new ConnectException("Connection refused")).obtenerServicio(1))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }
}
