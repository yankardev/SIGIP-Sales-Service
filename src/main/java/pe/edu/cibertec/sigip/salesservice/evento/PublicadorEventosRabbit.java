package pe.edu.cibertec.sigip.salesservice.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;

/**
 * Publica en RabbitMQ (exchange "sigip.pedidos"). Si el broker está caído, el pedido ya quedó guardado:
 * se registra la advertencia y la operación comercial no falla (comunicación asíncrona desacoplada).
 */
public class PublicadorEventosRabbit implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorEventosRabbit.class);

    private final RabbitTemplate rabbitTemplate;

    public PublicadorEventosRabbit(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(PedidoEvento evento) {
        try {
            rabbitTemplate.convertAndSend(PedidoEvento.EXCHANGE, PedidoEvento.ROUTING_KEY, evento);
            log.info("Evento {} publicado para el pedido {}", evento.tipo(), evento.idpedido());
        } catch (Exception e) {
            log.warn("No se pudo publicar el evento {} del pedido {}: {}", evento.tipo(), evento.idpedido(), e.getMessage());
        }
    }
}
