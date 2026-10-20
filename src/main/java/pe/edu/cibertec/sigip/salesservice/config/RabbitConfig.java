package pe.edu.cibertec.sigip.salesservice.config;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;
import pe.edu.cibertec.sigip.salesservice.evento.PublicadorEventos;
import pe.edu.cibertec.sigip.salesservice.evento.PublicadorEventosRabbit;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(PedidoEvento.EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    @ConditionalOnProperty(name = "sigip.eventos.habilitados", havingValue = "true", matchIfMissing = true)
    public PublicadorEventos publicadorRabbit(RabbitTemplate rabbitTemplate) {
        return new PublicadorEventosRabbit(rabbitTemplate);
    }

    /** Con sigip.eventos.habilitados=false (pruebas o nube sin broker) los eventos solo se registran en el log. */
    @Bean
    @ConditionalOnProperty(name = "sigip.eventos.habilitados", havingValue = "false")
    public PublicadorEventos publicadorLog() {
        var log = LoggerFactory.getLogger("sigip.eventos");
        return evento -> log.info("[eventos deshabilitados] {}", evento);
    }
}
