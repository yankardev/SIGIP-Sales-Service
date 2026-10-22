package pe.edu.cibertec.sigip.salesservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.dto.*;
import pe.edu.cibertec.sigip.salesservice.model.EstadoCotizacion;
import pe.edu.cibertec.sigip.salesservice.service.CotizacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService service;

    @GetMapping
    public ResponseEntity<List<CotizacionResponse>> listar(@RequestParam(required = false) EstadoCotizacion estado,
                                                           Authentication auth) {
        return ResponseEntity.ok(service.listar(UsuarioActual.de(auth), estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotizacionResponse> obtener(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(service.obtener(id, UsuarioActual.de(auth)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENTAS')")
    public ResponseEntity<CotizacionResponse> solicitar(@Valid @RequestBody CotizacionSolicitudRequest request,
                                                        Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(request, UsuarioActual.de(auth)));
    }

    @PutMapping("/{id}/precio")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS')")
    public ResponseEntity<CotizacionResponse> cotizar(@PathVariable Integer id,
                                                      @Valid @RequestBody CotizacionPrecioRequest request,
                                                      Authentication auth) {
        return ResponseEntity.ok(service.cotizar(id, request, UsuarioActual.de(auth)));
    }

    @PostMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENTAS')")
    public ResponseEntity<CotizacionResponse> decidir(@PathVariable Integer id,
                                                      @Valid @RequestBody CotizacionDecisionRequest request,
                                                      Authentication auth) {
        return ResponseEntity.ok(service.decidir(id, request, UsuarioActual.de(auth)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN','VENTAS')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
        service.eliminar(id, UsuarioActual.de(auth));
        return ResponseEntity.noContent().build();
    }
}
