package pe.edu.cibertec.sigip.salesservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas de la capa de acceso a datos de pedidos: insertar, actualizar, eliminar y listar. */
@DataJpaTest
@ActiveProfiles("test")
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void debeInsertarPedido() {
        Pedido guardado = pedidoRepository.save(pedido("cliente01", EstadoPedido.REGISTRADO));

        assertThat(guardado.getIdpedido()).isNotNull();
        assertThat(guardado.getVersion()).isNotNull();
    }

    @Test
    void debeActualizarPedido() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedido("cliente01", EstadoPedido.REGISTRADO));

        guardado.setCantidad(5);
        guardado.setTotal(new BigDecimal("475.00"));
        pedidoRepository.saveAndFlush(guardado);

        Pedido actualizado = pedidoRepository.findById(guardado.getIdpedido()).orElseThrow();
        assertThat(actualizado.getCantidad()).isEqualTo(5);
        assertThat(actualizado.getTotal()).isEqualByComparingTo("475.00");
    }

    @Test
    void debeEliminarPedido() {
        Pedido guardado = pedidoRepository.saveAndFlush(pedido("cliente01", EstadoPedido.REGISTRADO));

        pedidoRepository.deleteById(guardado.getIdpedido());
        pedidoRepository.flush();

        assertThat(pedidoRepository.findById(guardado.getIdpedido())).isEmpty();
    }

    @Test
    void debeListarPedidosPorCliente() {
        pedidoRepository.save(pedido("cliente01", EstadoPedido.REGISTRADO));
        pedidoRepository.save(pedido("cliente01", EstadoPedido.CONFIRMADO));
        pedidoRepository.save(pedido("cliente02", EstadoPedido.REGISTRADO));

        assertThat(pedidoRepository.findAll()).hasSize(3);
        assertThat(pedidoRepository.findByClienteOrderByFechaPedidoDesc("cliente01")).hasSize(2);
    }

    @Test
    void debeResumirPorEstadoEnLaBaseDeDatos() {
        pedidoRepository.save(pedido("cliente01", EstadoPedido.REGISTRADO));
        pedidoRepository.save(pedido("cliente01", EstadoPedido.CONFIRMADO));
        pedidoRepository.save(pedido("cliente02", EstadoPedido.CONFIRMADO));

        assertThat(pedidoRepository.contarPorEstado())
                .anyMatch(c -> c.getEstado() == EstadoPedido.CONFIRMADO && c.getCantidad() == 2);
        assertThat(pedidoRepository.sumarTotalPorEstados(EnumSet.of(EstadoPedido.CONFIRMADO)))
                .isEqualByComparingTo("190.00");
    }

    private Pedido pedido(String cliente, EstadoPedido estado) {
        return Pedido.builder()
                .cliente(cliente).idservicio(1).nombreServicio("Volantes").precioUnitario(new BigDecimal("95.00"))
                .cantidad(1).total(new BigDecimal("95.00")).estado(estado)
                .fechaPedido(LocalDateTime.now()).build();
    }
}
