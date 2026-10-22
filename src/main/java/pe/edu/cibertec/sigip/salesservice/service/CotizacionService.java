package pe.edu.cibertec.sigip.salesservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ReglaNegocioException;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.client.CatalogServiceClient;
import pe.edu.cibertec.sigip.salesservice.client.ServicioCatalogoResponse;
import pe.edu.cibertec.sigip.salesservice.dto.*;
import pe.edu.cibertec.sigip.salesservice.model.Cotizacion;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;
import pe.edu.cibertec.sigip.salesservice.model.Pedido;
import pe.edu.cibertec.sigip.salesservice.repository.CotizacionRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Flujo comercial para trabajos cuyo precio requiere evaluación antes de convertirse en pedido. */
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository repository;
    private final CatalogServiceClient catalogClient;
    private final PedidoService pedidoService;

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listar(UsuarioActual usuario, EstadoCotizacion estado) {
        List<Cotizacion> lista;
        if (!usuario.esInterno()) {
            lista = repository.findByClienteOrderByFechaSolicitudDesc(usuario.nombre());
        } else if (estado != null) {
            lista = repository.findByEstadoOrderByFechaSolicitudDesc(estado);
        } else {
            lista = repository.findAllByOrderByFechaSolicitudDesc();
        }
        return lista.stream().map(CotizacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtener(Integer id, UsuarioActual usuario) {
        return CotizacionResponse.from(buscarVisible(id, usuario));
    }

    @Transactional
    public CotizacionResponse solicitar(CotizacionSolicitudRequest request, UsuarioActual usuario) {
        ServicioCatalogoResponse servicio = catalogClient.obtenerServicio(request.idservicio());
        if (!Boolean.TRUE.equals(servicio.activo())) {
            throw new ReglaNegocioException("El servicio seleccionado no está disponible");
        }
        String cliente = usuario.esInterno() ? clienteInterno(request.cliente()) : usuario.nombre();

        Cotizacion c = Cotizacion.builder()
                .cliente(cliente)
                .idservicio(servicio.idservicio())
                .nombreServicio(servicio.nombre())
                .cantidad(request.cantidad())
                .especificaciones(request.especificaciones())
                .estado(EstadoCotizacion.SOLICITADA)
                .fechaSolicitud(LocalDateTime.now())
                .observacion(Boolean.TRUE.equals(servicio.requiereCotizacion())
                        ? "Pendiente de evaluación comercial" : "Cotización solicitada por el cliente")
                .build();
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public CotizacionResponse cotizar(Integer id, CotizacionPrecioRequest request, UsuarioActual usuario) {
        Cotizacion c = buscar(id);
        if (c.getEstado() != EstadoCotizacion.SOLICITADA) {
            throw new ReglaNegocioException("Solo se puede cotizar una solicitud pendiente");
        }
        c.setPrecioUnitario(request.precioUnitario());
        c.setTotal(request.precioUnitario().multiply(java.math.BigDecimal.valueOf(c.getCantidad())));
        c.setObservacion(request.observacion());
        c.setAtendidoPor(usuario.nombre());
        c.setEstado(EstadoCotizacion.COTIZADA);
        c.setFechaCotizacion(LocalDateTime.now());
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public CotizacionResponse decidir(Integer id, CotizacionDecisionRequest request, UsuarioActual usuario) {
        Cotizacion c = buscarVisible(id, usuario);
        if (c.getEstado() != EstadoCotizacion.COTIZADA) {
            throw new ReglaNegocioException("La cotización aún no está lista para decidirse");
        }
        c.setFechaDecision(LocalDateTime.now());
        if (!Boolean.TRUE.equals(request.aceptada())) {
            c.setEstado(EstadoCotizacion.RECHAZADA);
            return CotizacionResponse.from(repository.save(c));
        }

        Pedido pedido = pedidoService.registrarDesdeCotizacion(c);
        c.setIdpedido(pedido.getIdpedido());
        c.setEstado(EstadoCotizacion.ACEPTADA);
        return CotizacionResponse.from(repository.save(c));
    }

    @Transactional
    public void eliminar(Integer id, UsuarioActual usuario) {
        Cotizacion c = buscarVisible(id, usuario);
        if (c.getEstado() != EstadoCotizacion.SOLICITADA) {
            throw new ReglaNegocioException("Solo se puede eliminar una cotización que todavía no fue atendida");
        }
        repository.delete(c);
    }

    private Cotizacion buscar(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cotización no encontrada"));
    }

    private Cotizacion buscarVisible(Integer id, UsuarioActual usuario) {
        Cotizacion c = buscar(id);
        if (!usuario.esInterno() && !c.getCliente().equals(usuario.nombre())) {
            throw new RecursoNoEncontradoException("Cotización no encontrada");
        }
        return c;
    }

    private String clienteInterno(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            throw new ReglaNegocioException("Seleccione el cliente de la cotización");
        }
        return cliente.trim();
    }
}
