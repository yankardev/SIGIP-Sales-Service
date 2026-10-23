package pe.edu.cibertec.sigip.salesservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.common.error.RecursoNoEncontradoException;
import pe.edu.cibertec.sigip.common.error.ReglaNegocioException;
import pe.edu.cibertec.sigip.salesservice.dto.ClienteRequest;
import pe.edu.cibertec.sigip.salesservice.dto.ClienteResponse;
import pe.edu.cibertec.sigip.salesservice.model.Cliente;
import pe.edu.cibertec.sigip.salesservice.repository.ClienteRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository repository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Boolean soloActivos) {
        List<Cliente> lista = Boolean.TRUE.equals(soloActivos)
                ? repository.findByActivoTrueOrderByRazonSocialAsc()
                : repository.findAllByOrderByRazonSocialAsc();
        return lista.stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Integer id) {
        return ClienteResponse.from(buscar(id));
    }

    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {
        validarDocumento(request, null);
        LocalDateTime ahora = LocalDateTime.now();
        Cliente cliente = Cliente.builder()
                .tipoDocumento(normalizar(request.tipoDocumento()))
                .numeroDocumento(request.numeroDocumento().trim())
                .razonSocial(request.razonSocial().trim())
                .contacto(limpiar(request.contacto()))
                .telefono(limpiar(request.telefono()))
                .email(limpiar(request.email()))
                .direccion(limpiar(request.direccion()))
                .activo(request.activo() == null || request.activo())
                .fechaRegistro(ahora)
                .fechaActualizacion(ahora)
                .build();
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        validarDocumento(request, id);
        cliente.setTipoDocumento(normalizar(request.tipoDocumento()));
        cliente.setNumeroDocumento(request.numeroDocumento().trim());
        cliente.setRazonSocial(request.razonSocial().trim());
        cliente.setContacto(limpiar(request.contacto()));
        cliente.setTelefono(limpiar(request.telefono()));
        cliente.setEmail(limpiar(request.email()));
        cliente.setDireccion(limpiar(request.direccion()));
        if (request.activo() != null) cliente.setActivo(request.activo());
        cliente.setFechaActualizacion(LocalDateTime.now());
        return ClienteResponse.from(repository.save(cliente));
    }

    @Transactional
    public void desactivar(Integer id) {
        Cliente cliente = buscar(id);
        cliente.setActivo(false);
        cliente.setFechaActualizacion(LocalDateTime.now());
        repository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarActivo(Integer id) {
        Cliente cliente = buscar(id);
        if (!cliente.isActivo()) throw new ReglaNegocioException("El cliente seleccionado está inactivo");
        return cliente;
    }

    Cliente buscar(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    private void validarDocumento(ClienteRequest r, Integer id) {
        String tipo = normalizar(r.tipoDocumento());
        String numero = r.numeroDocumento().trim();
        boolean existe = id == null
                ? repository.existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCase(tipo, numero)
                : repository.existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCaseAndIdclienteNot(tipo, numero, id);
        if (existe) throw new ReglaNegocioException("Ya existe un cliente con ese documento");
        if ("DNI".equals(tipo) && !numero.matches("\\d{8}"))
            throw new ReglaNegocioException("El DNI debe contener 8 dígitos");
        if ("RUC".equals(tipo) && !numero.matches("\\d{11}"))
            throw new ReglaNegocioException("El RUC debe contener 11 dígitos");
    }

    private String normalizar(String valor) { return valor.trim().toUpperCase(); }
    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
