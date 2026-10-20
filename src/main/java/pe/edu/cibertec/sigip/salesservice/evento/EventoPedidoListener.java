package pe.edu.cibertec.sigip.salesservice.evento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;

/**
 * Envía el evento al broker solo DESPUÉS de que la transacción se confirmó (AFTER_COMMIT):
 * nunca se avisa de un cambio que finalmente no se guardó.
 */
@Component
@RequiredArgsConstructor
public class EventoPedidoListener {

    private final PublicadorEventos publicador;

    @TransactionalEventListener
    public void alConfirmar(PedidoEvento evento) {
        publicador.publicar(evento);
    }
}
