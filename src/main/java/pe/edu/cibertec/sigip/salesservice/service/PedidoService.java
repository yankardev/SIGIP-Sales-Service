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
import pe.edu.cibertec.sigip.salesservice.model.Cliente;
import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
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

/** Flujo central de SIGIP desde la venta hasta la entrega. */
@Service
@RequiredArgsConstructor
public class PedidoService {

    static final EnumSet<EstadoPedido> ESTADOS_CON_VENTA = EnumSet.of(EstadoPedido.CONFIRMADO,
            EstadoPedido.EN_DISENO, EstadoPedido.EN_PRODUCCION, EstadoPedido.TERMINADO, EstadoPedido.ENTREGADO);

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final CatalogServiceClient catalogClient;
    private final ClienteService clienteService;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioActual usuario, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (usuario.esClienteLegacy()) pedidos = pedidoRepository.findByClienteOrderByFechaPedidoDesc(usuario.nombre());
        else if (estado != null) pedidos = pedidoRepository.findByEstadoOrderByFechaPedidoDesc(estado);
        else pedidos = pedidoRepository.findAllByOrderByFechaPedidoDesc();
        return pedidos.stream().filter(p -> estado == null || p.getEstado() == estado).map(PedidoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Integer id, UsuarioActual usuario) {
        return PedidoResponse.from(buscarVisible(id, usuario));
    }

    @Transactional
    public PedidoResponse registrar(PedidoRequest request, UsuarioActual usuario) {
        ServicioCatalogoResponse servicio = servicioActivo(request.idservicio());
        Cliente cliente = resolverCliente(request.idcliente(), usuario);
        BigDecimal precio = resolverPrecio(servicio, request);
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .idcliente(cliente == null ? null : cliente.getIdcliente())
                .cliente(cliente == null ? usuario.nombre() : cliente.getRazonSocial())
                .registradoPor(usuario.nombre())
                .idservicio(servicio.idservicio())
                .nombreServicio(servicio.nombre())
                .precioUnitario(precio)
                .cantidad(request.cantidad())
                .total(calcularTotal(precio, request.cantidad()))
                .estado(EstadoPedido.REGISTRADO)
                .especificaciones(request.especificaciones())
                .detallePrecio(detallePrecio(servicio, request, precio))
                .observacion(null)
                .fechaPedido(ahora)
                .fechaEntrega(request.fechaEntrega())
                .fechaActualizacion(ahora)
                .build();
        Pedido guardado = pedidoRepository.save(pedido);
        publicar("PEDIDO_REGISTRADO", guardado);
        return PedidoResponse.from(guardado);
    }

    /** Crea el pedido respetando el precio ya acordado en una cotización aprobada. */
    @Transactional
    public PedidoResponse registrarDesdeCotizacion(Cotizacion c, UsuarioActual usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .idcliente(c.getIdcliente())
                .cliente(c.getCliente())
                .registradoPor(usuario.nombre())
                .idcotizacion(c.getIdcotizacion())
                .idservicio(c.getIdservicio())
                .nombreServicio(c.getNombreServicio())
                .precioUnitario(c.getPrecioUnitario())
                .cantidad(c.getCantidad())
                .total(c.getTotal())
                .estado(EstadoPedido.REGISTRADO)
                .especificaciones(c.getEspecificaciones())
                .detallePrecio("Precio acordado en cotización #" + c.getIdcotizacion() + ": S/ " + c.getPrecioUnitario())
                .observacion("Pedido generado desde cotización #" + c.getIdcotizacion())
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
        BigDecimal precio = resolverPrecio(servicio, request);
        if (!usuario.esClienteLegacy()) {
            Cliente cliente = clienteService.buscarActivo(request.idcliente());
            pedido.setIdcliente(cliente.getIdcliente());
            pedido.setCliente(cliente.getRazonSocial());
        }
        pedido.setIdservicio(servicio.idservicio());
        pedido.setNombreServicio(servicio.nombre());
        pedido.setPrecioUnitario(precio);
        pedido.setCantidad(request.cantidad());
        pedido.setTotal(calcularTotal(precio, request.cantidad()));
        pedido.setEspecificaciones(request.especificaciones());
        pedido.setDetallePrecio(detallePrecio(servicio, request, precio));
        pedido.setFechaEntrega(request.fechaEntrega());
        pedido.setFechaActualizacion(LocalDateTime.now());
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional
    public void eliminar(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscarVisible(id, usuario);
        exigirEstado(pedido, EstadoPedido.REGISTRADO, "Solo se puede eliminar un pedido REGISTRADO; use la cancelación");
        if (pagoRepository.existsByPedidoIdpedido(id)) throw new ReglaNegocioException("El pedido tiene pagos registrados; use la cancelación");
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

    @Transactional
    public PedidoResponse registrarEntrega(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscarVisible(id, usuario);
        exigirEstado(pedido, EstadoPedido.TERMINADO, "Solo un trabajo TERMINADO puede registrarse como entregado");
        aplicarTransicion(pedido, EstadoPedido.ENTREGADO, "Entrega registrada por " + usuario.nombre());
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public ResumenVentasResponse resumen() {
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoPedido e : EstadoPedido.values()) porEstado.put(e.name(), 0L);
        long total = 0;
        for (var conteo : pedidoRepository.contarPorEstado()) {
            porEstado.put(conteo.getEstado().name(), conteo.getCantidad());
            total += conteo.getCantidad();
        }
        return new ResumenVentasResponse(total, porEstado, pedidoRepository.sumarTotalPorEstados(ESTADOS_CON_VENTA));
    }

    void aplicarTransicion(Pedido pedido, EstadoPedido destino, String observacion) {
        if (!pedido.getEstado().puedePasarA(destino))
            throw new ReglaNegocioException("No se puede pasar de " + pedido.getEstado() + " a " + destino + ". Estados permitidos: " + pedido.getEstado().siguientes());
        pedido.setEstado(destino);
        if (observacion != null && !observacion.isBlank()) pedido.setObservacion(observacion);
        pedido.setFechaActualizacion(LocalDateTime.now());
        publicar("PEDIDO_" + destino.name(), pedido);
    }

    Pedido buscar(Integer id) {
        return pedidoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    Pedido buscarVisible(Integer id, UsuarioActual usuario) {
        Pedido pedido = buscar(id);
        if (usuario.esClienteLegacy() && !pedido.getCliente().equals(usuario.nombre())) throw new RecursoNoEncontradoException("Pedido no encontrado");
        return pedido;
    }

    private Cliente resolverCliente(Integer idcliente, UsuarioActual usuario) {
        if (usuario.esClienteLegacy() && idcliente == null) return null;
        if (idcliente == null) throw new ReglaNegocioException("Seleccione un cliente registrado antes de crear el pedido");
        return clienteService.buscarActivo(idcliente);
    }

    private void exigirEstado(Pedido pedido, EstadoPedido esperado, String mensaje) {
        if (pedido.getEstado() != esperado) throw new ReglaNegocioException(mensaje);
    }

    private ServicioCatalogoResponse servicioActivo(Integer idservicio) {
        ServicioCatalogoResponse servicio = catalogClient.obtenerServicio(idservicio);
        if (!Boolean.TRUE.equals(servicio.activo())) throw new ReglaNegocioException("El servicio seleccionado no está disponible");
        return servicio;
    }

    private BigDecimal resolverPrecio(ServicioCatalogoResponse servicio, PedidoRequest request) {
        if (!Boolean.TRUE.equals(servicio.personalizable())) return servicio.precioBase();
        if (request.precioUnitarioAcordado() == null)
            throw new ReglaNegocioException("Seleccione las opciones del servicio para calcular el precio final");
        if (request.precioUnitarioAcordado().compareTo(servicio.precioBase()) < 0)
            throw new ReglaNegocioException("El precio personalizado no puede ser menor al precio base del catálogo");
        if (request.detallePrecio() == null || request.detallePrecio().isBlank())
            throw new ReglaNegocioException("Debe enviarse el detalle de las opciones que forman el precio personalizado");
        return request.precioUnitarioAcordado();
    }

    private String detallePrecio(ServicioCatalogoResponse servicio, PedidoRequest request, BigDecimal precio) {
        if (Boolean.TRUE.equals(servicio.personalizable())) return request.detallePrecio();
        return "Precio fijo de catálogo: S/ " + precio;
    }

    private BigDecimal calcularTotal(BigDecimal precio, Integer cantidad) {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    private void publicar(String tipo, Pedido pedido) {
        String destinatarioInterno = pedido.getRegistradoPor() != null ? pedido.getRegistradoPor() : pedido.getCliente();
        eventos.publishEvent(PedidoEvento.de(tipo, pedido.getIdpedido(), destinatarioInterno, pedido.getEstado().name(), pedido.getTotal()));
    }
}
