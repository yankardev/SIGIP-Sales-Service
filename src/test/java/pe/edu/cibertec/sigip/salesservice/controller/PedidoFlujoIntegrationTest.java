package pe.edu.cibertec.sigip.salesservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pe.edu.cibertec.sigip.common.security.JwtService;
import pe.edu.cibertec.sigip.salesservice.client.CatalogServiceClient;
import pe.edu.cibertec.sigip.salesservice.client.ServicioCatalogoResponse;
import pe.edu.cibertec.sigip.salesservice.repository.PagoRepository;
import pe.edu.cibertec.sigip.salesservice.repository.PedidoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo de un pedido vía REST: registro → pago → verificación → producción → entrega.
 * El catálogo se simula (MockitoBean) para probar sales-service de forma aislada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoFlujoIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private PagoRepository pagoRepository;

    @MockitoBean
    private CatalogServiceClient catalogClient;

    @BeforeEach
    void setUp() {
        pagoRepository.deleteAll();
        pedidoRepository.deleteAll();
        when(catalogClient.obtenerServicio(1)).thenReturn(new ServicioCatalogoResponse(1, "Volantes A5",
                "Couché", new BigDecimal("95.00"), false, true, 1, "Digital"));
        when(catalogClient.obtenerServicio(2)).thenReturn(new ServicioCatalogoResponse(2, "Descontinuado",
                null, new BigDecimal("10.00"), false, false, 1, "Digital"));
    }

    private String token(String usuario, String rol) {
        return "Bearer " + jwtService.generarToken(usuario, List.of("ROLE_" + rol));
    }

    private ResultActions registrarPedido(String usuario, String body) throws Exception {
        return mvc.perform(post("/api/v1/pedidos").header("Authorization", token(usuario, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private Integer idDe(ResultActions resultado, String campo) throws Exception {
        String json = resultado.andReturn().getResponse().getContentAsString();
        String clave = "\"" + campo + "\":";
        int inicio = json.indexOf(clave) + clave.length();
        int fin = inicio;
        while (Character.isDigit(json.charAt(fin))) {
            fin++;
        }
        return Integer.valueOf(json.substring(inicio, fin));
    }

    @Test
    @DisplayName("El total lo calcula el servidor con el precio del catálogo (Feign)")
    void registrarCalculaTotal() throws Exception {
        registrarPedido("cliente01", "{\"idservicio\":1,\"cantidad\":3,\"especificaciones\":\"Logo a color\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente").value("cliente01"))
                .andExpect(jsonPath("$.total").value(285.00))
                .andExpect(jsonPath("$.estado").value("REGISTRADO"));
    }

    @Test
    @DisplayName("Servicio inactivo (409) y validaciones (400)")
    void reglasDeRegistro() throws Exception {
        registrarPedido("cliente01", "{\"idservicio\":2,\"cantidad\":1}").andExpect(status().isConflict());
        registrarPedido("cliente01", "{\"idservicio\":1,\"cantidad\":0}").andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idservicio\":1,\"cantidad\":1}")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un cliente no puede ver ni borrar el pedido de otro")
    void propiedadDelPedido() throws Exception {
        Integer id = idDe(registrarPedido("cliente01", "{\"idservicio\":1,\"cantidad\":1}"), "idpedido");

        mvc.perform(get("/api/v1/pedidos/" + id).header("Authorization", token("cliente02", "CLIENTE")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/pedidos/" + id).header("Authorization", token("cliente02", "CLIENTE")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/pedidos").header("Authorization", token("cliente02", "CLIENTE")))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/pedidos/" + id).header("Authorization", token("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT y DELETE solo mientras el pedido está REGISTRADO")
    void editarYEliminar() throws Exception {
        Integer id = idDe(registrarPedido("cliente01", "{\"idservicio\":1,\"cantidad\":1}"), "idpedido");

        mvc.perform(put("/api/v1/pedidos/" + id).header("Authorization", token("cliente01", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idservicio\":1,\"cantidad\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(190.00));

        mvc.perform(delete("/api/v1/pedidos/" + id).header("Authorization", token("cliente01", "CLIENTE")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Flujo completo: pago, verificación, producción y entrega; saltos inválidos dan 409")
    void flujoCompleto() throws Exception {
        String cliente = token("cliente01", "CLIENTE");
        String admin = token("admin", "ADMIN");
        Integer id = idDe(registrarPedido("cliente01", "{\"idservicio\":1,\"cantidad\":2}"), "idpedido");

        // Saltar directo a ENTREGADO no está permitido
        mvc.perform(patch("/api/v1/pedidos/" + id + "/estado").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isConflict());

        // Pago insuficiente
        mvc.perform(post("/api/v1/pedidos/" + id + "/pagos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":50,\"metodo\":\"YAPE\",\"numeroOperacion\":\"OP-1\"}"))
                .andExpect(status().isConflict());

        Integer idpago = idDe(mvc.perform(post("/api/v1/pedidos/" + id + "/pagos").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":190,\"metodo\":\"YAPE\",\"numeroOperacion\":\"OP-2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE")), "idpago");

        // Un CLIENTE no puede verificar pagos
        mvc.perform(post("/api/v1/pagos/" + idpago + "/verificacion").header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/pagos/" + idpago + "/verificacion").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprobado\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("VERIFICADO"));

        for (String estado : List.of("EN_PRODUCCION", "TERMINADO", "ENTREGADO")) {
            mvc.perform(patch("/api/v1/pedidos/" + id + "/estado").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"" + estado + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value(estado));
        }

        mvc.perform(get("/api/v1/pedidos/resumen").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidosPorEstado.ENTREGADO").value(1))
                .andExpect(jsonPath("$.ventasConfirmadas").value(190.00));
    }
}
