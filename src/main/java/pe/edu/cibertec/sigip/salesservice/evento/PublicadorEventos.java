package pe.edu.cibertec.sigip.salesservice.evento;

import pe.edu.cibertec.sigip.common.evento.PedidoEvento;

/** Abstracción para publicar eventos de pedido (permite cambiar RabbitMQ por otro broker sin tocar la lógica). */
public interface PublicadorEventos {

    void publicar(PedidoEvento evento);
}
