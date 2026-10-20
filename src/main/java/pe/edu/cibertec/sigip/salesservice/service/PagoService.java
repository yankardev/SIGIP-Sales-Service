package pe.edu.cibertec.sigip.salesservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.common.error.ReglaNegocioException;
import pe.edu.cibertec.sigip.salesservice.dto.PagoRequest;
import pe.edu.cibertec.sigip.salesservice.dto.PagoResponse;
import pe.edu.cibertec.sigip.salesservice.dto.VerificacionPagoRequest;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPago;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pago;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;
import pe.edu.cibertec.sigip.salesservice.repository.PagoRepository;
import pe.edu.cibertec.sigip.salesservice.repository.PedidoRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pagos verificados manualmente (el proyecto no integra una pasarela bancaria real):
 * el cliente registra el comprobante y el personal autorizado lo aprueba o rechaza.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;

    @Transactional
    public PagoResponse registrar(Integer idpedido, PagoRequest request, UsuarioActual usuario) {
        Pedido pedido = pedidoService.buscarVisible(idpedido, usuario);
        if (pedido.getEstado() != EstadoPedido.REGISTRADO) {
            throw new ReglaNegocioException("Solo se registra el pago de un pedido en estado REGISTRADO");
        }
        if (request.monto().compareTo(pedido.getTotal()) < 0) {
            throw new ReglaNegocioException("El monto no cubre el total del pedido (" + pedido.getTotal() + ")");
        }

        Pago pago = pagoRepository.save(Pago.builder()
                .pedido(pedido)
                .monto(request.monto())
                .metodo(request.metodo())
                .numeroOperacion(request.numeroOperacion())
                .estado(EstadoPago.PENDIENTE)
                .fechaRegistro(LocalDateTime.now())
                .build());

        pedidoService.aplicarTransicion(pedido, EstadoPedido.PAGO_EN_REVISION, "Pago registrado, pendiente de verificación");
        pedidoRepository.save(pedido);
        return PagoResponse.from(pago);
    }

    @Transactional
    public PagoResponse verificar(Integer idpago, VerificacionPagoRequest request, UsuarioActual usuario) {
        Pago pago = pagoRepository.findById(idpago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("El pago ya fue verificado");
        }

        boolean aprobado = request.aprobado();
        pago.setEstado(aprobado ? EstadoPago.VERIFICADO : EstadoPago.RECHAZADO);
        pago.setFechaVerificacion(LocalDateTime.now());
        pago.setVerificadoPor(usuario.nombre());
        pago.setObservacion(request.observacion());

        Pedido pedido = pago.getPedido();
        pedidoService.aplicarTransicion(pedido,
                aprobado ? EstadoPedido.CONFIRMADO : EstadoPedido.REGISTRADO,
                aprobado ? "Pago verificado" : "Pago rechazado: " + request.observacion());
        pedidoRepository.save(pedido);

        return PagoResponse.from(pagoRepository.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorPedido(Integer idpedido, UsuarioActual usuario) {
        pedidoService.buscarVisible(idpedido, usuario);
        return pagoRepository.findByPedidoIdpedidoOrderByFechaRegistroDesc(idpedido).stream()
                .map(PagoResponse::from)
                .toList();
    }
}
