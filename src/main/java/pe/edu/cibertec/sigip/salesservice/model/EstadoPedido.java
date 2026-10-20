package pe.edu.cibertec.sigip.salesservice.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida del pedido (patrón State simplificado).
 * Cada estado conoce a qué estados puede pasar; así no se puede, por ejemplo,
 * marcar como ENTREGADO un pedido que nunca se produjo.
 */
public enum EstadoPedido {
    REGISTRADO,
    PAGO_EN_REVISION,
    CONFIRMADO,
    EN_DISENO,
    EN_PRODUCCION,
    TERMINADO,
    ENTREGADO,
    CANCELADO;

    public Set<EstadoPedido> siguientes() {
        return switch (this) {
            case REGISTRADO -> EnumSet.of(PAGO_EN_REVISION, CANCELADO);
            case PAGO_EN_REVISION -> EnumSet.of(CONFIRMADO, REGISTRADO, CANCELADO);
            case CONFIRMADO -> EnumSet.of(EN_DISENO, EN_PRODUCCION, CANCELADO);
            case EN_DISENO -> EnumSet.of(EN_PRODUCCION);
            case EN_PRODUCCION -> EnumSet.of(TERMINADO);
            case TERMINADO -> EnumSet.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> EnumSet.noneOf(EstadoPedido.class);
        };
    }

    public boolean puedePasarA(EstadoPedido destino) {
        return siguientes().contains(destino);
    }
}
