package pe.edu.cibertec.sigip.salesservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.sigip.common.security.UsuarioActual;
import pe.edu.cibertec.sigip.salesservice.dto.PagoResponse;
import pe.edu.cibertec.sigip.salesservice.dto.VerificacionPagoRequest;
import pe.edu.cibertec.sigip.salesservice.service.PagoService;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /** La verificación pertenece al proceso comercial, por eso la realiza ADMIN o VENTAS. */
    @PostMapping("/{id}/verificacion")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS')")
    public ResponseEntity<PagoResponse> verificar(@PathVariable Integer id,
                                                  @Valid @RequestBody VerificacionPagoRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(pagoService.verificar(id, request, UsuarioActual.de(auth)));
    }
}
