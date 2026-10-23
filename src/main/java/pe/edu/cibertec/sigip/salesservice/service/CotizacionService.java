package pe.edu.cibertec.sigip.salesservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ReglaNegocioException;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.client.CatalogServiceClient;
import pe.edu.cibertec.sigip.salesservice.client.ServicioCatalogoResponse;
import pe.edu.cibertec.sigip.salesservice.dto.CotizacionRequest;
import pe.edu.cibertec.sigip.salesservice.dto.CotizacionResponse;
import pe.edu.cibertec.sigip.salesservice.dto.PedidoResponse;
import pe.edu.cibertec.sigip.salesservice.model.Cliente;
import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;
import pe.edu.cibertec.sigip.salesservice.repository.CotizacionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotizacionService {
    private final CotizacionRepository repository;
    private final ClienteService clienteService;
    private final CatalogServiceClient catalogClient;
    private final PedidoService pedidoService;

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listar(EstadoCotizacion estado, Integer idcliente) {
        List<Cotizacion> lista = idcliente != null
                ? repository.findByIdclienteOrderByFechaRegistroDesc(idcliente)
                : estado != null
                    ? repository.findByEstadoOrderByFechaRegistroDesc(estado)
                    : repository.findAllByOrderByFechaRegistroDesc();
        return lista.stream().map(this::actualizarVencimientoLectura).map(CotizacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtener(Integer id) {
        return CotizacionResponse.from(actualizarVencimientoLectura(buscar(id)));
    }

    @Transactional
    public CotizacionResponse registrar(CotizacionRequest request, UsuarioActual usuario) {
        Cliente cliente = clienteService.buscarActivo(request.idcliente());
        ServicioCatalogoResponse servicio = catalogClient.obtenerServicio(request.idservicio());
        if (!Boolean.TRUE.equals(servicio.activo())) throw new ReglaNegocioException("El servicio seleccionado está inactivo");

        BigDecimal precio = request.precioUnitarioAcordado() != null
                ? request.precioUnitarioAcordado()
                : servicio.precioBase();
        LocalDateTime ahora = LocalDateTime.now();
        Cotizacion c = Cotizacion.builder()
                .idcliente(cliente.getIdcliente())
                .cliente(cliente.getRazonSocial())
                .idservicio(servicio.idservicio())
                .nombreServicio(servicio.nombre())
                .precioUnitario(precio)
                .cantidad(request.cantidad())
                .total(precio.multiply(BigDecimal.valueOf(request.cantidad())))
                .especificaciones(request.especificaciones())
                .vigenciaHasta(request.vigenciaHasta() != null ? request.vigenciaHasta() : LocalDate.now().plusDays(7))
                .estado(EstadoCotizacion.BORRADOR)
                .creadoPor(usuario.nombre())
                .fechaRegistro(ahora)
                .fechaActualizacion(ahora)
                .build();
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public CotizacionResponse actualizar(Integer id, CotizacionRequest request) {
        Cotizacion c = buscar(id);
        exigirEditable(c);
        Cliente cliente = clienteService.buscarActivo(request.idcliente());
        ServicioCatalogoResponse servicio = catalogClient.obtenerServicio(request.idservicio());
        BigDecimal precio = request.precioUnitarioAcordado() != null ? request.precioUnitarioAcordado() : servicio.precioBase();
        c.setIdcliente(cliente.getIdcliente());
        c.setCliente(cliente.getRazonSocial());
        c.setIdservicio(servicio.idservicio());
        c.setNombreServicio(servicio.nombre());
        c.setPrecioUnitario(precio);
        c.setCantidad(request.cantidad());
        c.setTotal(precio.multiply(BigDecimal.valueOf(request.cantidad())));
        c.setEspecificaciones(request.especificaciones());
        c.setVigenciaHasta(request.vigenciaHasta());
        c.setFechaActualizacion(LocalDateTime.now());
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public CotizacionResponse aprobar(Integer id) {
        Cotizacion c = buscar(id);
        actualizarVencimiento(c);
        if (c.getEstado() == EstadoCotizacion.VENCIDA) throw new ReglaNegocioException("La cotización está vencida");
        if (c.getEstado() != EstadoCotizacion.BORRADOR) throw new ReglaNegocioException("Solo una cotización en BORRADOR puede aprobarse");
        c.setEstado(EstadoCotizacion.APROBADA);
        c.setFechaActualizacion(LocalDateTime.now());
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public CotizacionResponse rechazar(Integer id) {
        Cotizacion c = buscar(id);
        if (c.getEstado() == EstadoCotizacion.CONVERTIDA) throw new ReglaNegocioException("Una cotización convertida no puede rechazarse");
        c.setEstado(EstadoCotizacion.RECHAZADA);
        c.setFechaActualizacion(LocalDateTime.now());
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public PedidoResponse convertir(Integer id, UsuarioActual usuario) {
        Cotizacion c = buscar(id);
        actualizarVencimiento(c);
        if (c.getEstado() != EstadoCotizacion.APROBADA) throw new ReglaNegocioException("Apruebe la cotización antes de convertirla en pedido");
        PedidoResponse pedido = pedidoService.registrarDesdeCotizacion(c, usuario);
        c.setEstado(EstadoCotizacion.CONVERTIDA);
        c.setFechaActualizacion(LocalDateTime.now());
        repository.save(c);
        return pedido;
    }

    @Transactional
    public void eliminar(Integer id) {
        Cotizacion c = buscar(id);
        exigirEditable(c);
        repository.delete(c);
    }

    private Cotizacion buscar(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cotización no encontrada"));
    }

    private void exigirEditable(Cotizacion c) {
        if (c.getEstado() != EstadoCotizacion.BORRADOR)
            throw new ReglaNegocioException("Solo se puede editar o eliminar una cotización en BORRADOR");
    }

    private Cotizacion actualizarVencimientoLectura(Cotizacion c) {
        actualizarVencimiento(c);
        return c;
    }

    private void actualizarVencimiento(Cotizacion c) {
        if ((c.getEstado() == EstadoCotizacion.BORRADOR || c.getEstado() == EstadoCotizacion.APROBADA)
                && c.getVigenciaHasta() != null && c.getVigenciaHasta().isBefore(LocalDate.now())) {
            c.setEstado(EstadoCotizacion.VENCIDA);
        }
    }
}
