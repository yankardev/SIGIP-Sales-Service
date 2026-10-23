package pe.edu.cibertec.sigip.salesservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.dto.CotizacionRequest;
import pe.edu.cibertec.sigip.salesservice.dto.CotizacionResponse;
import pe.edu.cibertec.sigip.salesservice.dto.PedidoResponse;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;
import pe.edu.cibertec.sigip.salesservice.service.CotizacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','VENTAS')")
public class CotizacionController {
    private final CotizacionService service;

    @GetMapping
    public ResponseEntity<List<CotizacionResponse>> listar(@RequestParam(required = false) EstadoCotizacion estado,
                                                           @RequestParam(required = false) Integer idcliente) {
        return ResponseEntity.ok(service.listar(estado, idcliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotizacionResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> registrar(@Valid @RequestBody CotizacionRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request, UsuarioActual.de(auth)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CotizacionResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody CotizacionRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/aprobacion")
    public ResponseEntity<CotizacionResponse> aprobar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.aprobar(id));
    }

    @PostMapping("/{id}/rechazo")
    public ResponseEntity<CotizacionResponse> rechazar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.rechazar(id));
    }

    @PostMapping("/{id}/conversion")
    public ResponseEntity<PedidoResponse> convertir(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.convertir(id, UsuarioActual.de(auth)));
    }
}
