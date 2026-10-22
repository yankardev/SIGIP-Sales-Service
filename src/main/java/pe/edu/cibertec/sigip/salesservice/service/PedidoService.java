package pe.edu.cibertec.sigip.salesservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ReglaNegocioException;
import pe.edu.cibertec.sigip.common.evento.PedidoEvento;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.client.CatalogServiceClient;
import pe.edu.cibertec.sigip.salesservice.client.ServicioCatalogoResponse;
import pe.edu.cibertec.sigip.salesservice.dto.*;
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;
import pe.edu.cibertec.sigip.salesservice.repository.PagoRepository;
import pe.edu.cibertec.sigip.salesservice.repository.PedidoRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reglas del pedido:
 * - El precio se obtiene del catálogo (Feign), nunca del navegador.
 * - CLIENTE solo consulta y modifica sus propios pedidos.
 * - ADMIN, VENTAS y OPERACIONES pueden consultar los pedidos de la empresa.
 * - ADMIN/VENTAS pueden registrar un pedido de mostrador indicando el usuario CLIENTE.
 * - Solo se edita o elimina un pedido REGISTRADO (aún sin pagos).
 * - Los cambios de estado siguen las transiciones permitidas de {@link EstadoPedido}.
 */
@Service
@RequiredArgsConstructor
public class PedidoService {

    static final EnumSet<EstadoPedido> ESTADOS_CON_VENTA = EnumSet.of(EstadoPedido.CONFIRMADO,
            EstadoPedido.EN_DISENO, EstadoPedido.EN_PRODUCCION, EstadoPedido.TERMINADO, EstadoPedido.ENTREGADO);

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final CatalogServiceClient catalogClient;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioActual usuario, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (!usuario.esInterno()) {
            pedidos = pedidoRepository.findByClienteOrderByFechaPedidoDesc(usuario.nombre());
        } else if (estado != null) {
            pedidos = pedidoRepository.findByEstadoOrderByFechaPedidoDesc(estado);
        } else {
            pedidos = pedidoRepository.findAllByOrderByFechaPedidoDesc();
        }
        return pedidos.stream()
                .filter(p -> estado == null || p.getEstado() == estado)
                .map(PedidoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Integer id, UsuarioActual usuario) {
        return PedidoResponse.from(buscarVisible(id, usuario));
    }

    @Transactional
    public PedidoResponse registrar(PedidoRequest request, UsuarioActual usuario) {
        ServicioCatalogoResponse servicio = servicioActivo(request.idservicio());
        LocalDateTime ahora = LocalDateTime.now();
        String cliente = resolverCliente(request, usuario);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .idservicio(servicio.idservicio())
                .nombreServicio(servicio.nombre())
                .precioUnitario(servicio.precioBase())
                .cantidad(request.cantidad())
                .total(calcularTotal(servicio.precioBase(), request.cantidad()))
                .estado(EstadoPedido.REGISTRADO)
                .especificaciones(request.especificaciones())
                .observacion(Boolean.TRUE.equals(servicio.requiereCotizacion())
                        ? "Precio referencial: el servicio requiere cotización comercial" : null)
                .fechaPedido(ahora)
                .fechaActualizacion(ahora)
                .build();

        Pedido guardado = pedidoRepository.save(pedido);
        publicar("PEDIDO_REGISTRADO", guardado);
        return PedidoResponse.from(guardado);
    }

    @Transactional
    public PedidoResponse actualizar(Integer id, PedidoRequest request, UsuarioActual usuario) {
        Pedido pedido = buscarVisible(id, usuario);
        exigirEstado(pedido, EstadoPedido.REGISTRADO, "Solo se puede modificar un pedido en estado REGISTRADO");

        ServicioCatalogoResponse servicio = servicioActivo(request.idservicio());
        pedido.setIdservicio(servicio.idservicio());
        pedido.setNombreServicio(servicio.nombre());
        pedido.setPrecioUnitario(servicio.precioBase());
        pedido.setCantidad(request.cantidad());
        pedido.setTotal(calcularTotal(servicio.precioBase(), request.cantidad()));
        pedido.setEspecificaciones(request.especificaciones());
        pedido.setFechaActualizacion(LocalDateTime.now());

        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public void eliminar(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscarVisible(id, usuario);
        exigirEstado(pedido, EstadoPedido.REGISTRADO, "Solo se puede eliminar un pedido REGISTRADO; use la cancelación");
        if (pagoRepository.existsByPedidoIdpedido(id)) {
            throw new ReglaNegocioException("El pedido tiene pagos registrados; use la cancelación");
        }
        pedidoRepository.delete(pedido);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Integer id, CambioEstadoRequest request) {
        Pedido pedido = buscar(id);
        aplicarTransicion(pedido, request.estado(), request.observacion());
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cancelar(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscarVisible(id, usuario);
        aplicarTransicion(pedido, EstadoPedido.CANCELADO, "Cancelado por " + usuario.nombre());
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public ResumenVentasResponse resumen() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoPedido e : EstadoPedido.values()) {
            porEstado.put(e.name(), 0L);
        }
        long total = 0;
        for (var conteo : pedidoRepository.contarPorEstado()) {
            porEstado.put(conteo.getEstado().name(), conteo.getCantidad());
            total += conteo.getCantidad();
        }
        return new ResumenVentasResponse(total, porEstado, pedidoRepository.sumarTotalPorEstados(ESTADOS_CON_VENTA));
    }

    void aplicarTransicion(Pedido pedido, EstadoPedido destino, String observacion) {
        if (!pedido.getEstado().puedePasarA(destino)) {
            throw new ReglaNegocioException("No se puede pasar de " + pedido.getEstado() + " a " + destino
                    + ". Estados permitidos: " + pedido.getEstado().siguientes());
        }
        pedido.setEstado(destino);
        if (observacion != null && !observacion.isBlank()) {
            pedido.setObservacion(observacion);
        }
        pedido.setFechaActualizacion(LocalDateTime.now());
        publicar("PEDIDO_" + destino.name(), pedido);
    }

    Pedido buscar(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    /** Un cliente no puede descubrir pedidos ajenos. El personal interno sí puede consultarlos. */
    Pedido buscarVisible(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscar(id);
        if (!usuario.esInterno() && !pedido.getCliente().equals(usuario.nombre())) {
            throw new RecursoNoEncontradoException("Pedido no encontrado");
        }
        return pedido;
    }

    private String resolverCliente(PedidoRequest request, UsuarioActual usuario) {
        if (!usuario.esInterno()) {
            return usuario.nombre();
        }
        if (request.cliente() == null || request.cliente().isBlank()) {
            throw new ReglaNegocioException("Seleccione el cliente para registrar el pedido");
        }
        return request.cliente().trim();
    }

    private void exigirEstado(Pedido pedido, EstadoPedido esperado, String mensaje) {
        if (pedido.getEstado() != esperado) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private ServicioCatalogoResponse servicioActivo(Integer idservicio) {
        ServicioCatalogoResponse servicio = catalogClient.obtenerServicio(idservicio);
        if (!Boolean.TRUE.equals(servicio.activo())) {
            throw new ReglaNegocioException("El servicio seleccionado no está disponible");
        }
        return servicio;
    }

    private BigDecimal calcularTotal(BigDecimal precio, Integer cantidad) {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    private void publicar(String tipo, Pedido pedido) {
        eventos.publishEvent(PedidoEvento.de(tipo, pedido.getIdpedido(), pedido.getCliente(),
                pedido.getEstado().name(), pedido.getTotal()));
    }
}
