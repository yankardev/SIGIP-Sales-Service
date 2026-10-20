package pe.edu.cibertec.sigip.salesservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoPedidoTest {

    @Test
    void flujoNormalPermitido() {
        assertThat(EstadoPedido.REGISTRADO.puedePasarA(EstadoPedido.PAGO_EN_REVISION)).isTrue();
        assertThat(EstadoPedido.PAGO_EN_REVISION.puedePasarA(EstadoPedido.CONFIRMADO)).isTrue();
        assertThat(EstadoPedido.CONFIRMADO.puedePasarA(EstadoPedido.EN_PRODUCCION)).isTrue();
        assertThat(EstadoPedido.EN_PRODUCCION.puedePasarA(EstadoPedido.TERMINADO)).isTrue();
        assertThat(EstadoPedido.TERMINADO.puedePasarA(EstadoPedido.ENTREGADO)).isTrue();
    }

    @Test
    void saltosNoPermitidos() {
        assertThat(EstadoPedido.REGISTRADO.puedePasarA(EstadoPedido.ENTREGADO)).isFalse();
        assertThat(EstadoPedido.EN_PRODUCCION.puedePasarA(EstadoPedido.CANCELADO)).isFalse();
        assertThat(EstadoPedido.ENTREGADO.siguientes()).isEmpty();
        assertThat(EstadoPedido.CANCELADO.siguientes()).isEmpty();
    }
}
