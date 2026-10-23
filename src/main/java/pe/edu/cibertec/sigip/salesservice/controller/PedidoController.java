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
import pe.edu.cibertec.sigip.salesservice.model.EstadoPedido;
import pe.edu.cibertec.sigip.salesservice.service.PagoService;
import pe.edu.cibertec.sigip.salesservice.service.PedidoService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','OPERACIONES','CLIENTE')")
    public ResponseEntity<List<PedidoResponse>> listar(@RequestParam(required = false) EstadoPedido estado,
                                                       Authentication auth) {
        return ResponseEntity.ok(pedidoService.listar(UsuarioActual.de(auth), estado));
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','OPERACIONES')")
    public ResponseEntity<ResumenVentasResponse> resumen() {
        return ResponseEntity.ok(pedidoService.resumen());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','OPERACIONES','CLIENTE')")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(pedidoService.obtener(id, UsuarioActual.de(auth)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','CLIENTE')")
    public ResponseEntity<PedidoResponse> registrar(@Valid @RequestBody PedidoRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.registrar(request, UsuarioActual.de(auth)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','CLIENTE')")
    public ResponseEntity<PedidoResponse> actualizar(@PathVariable Integer id,
                                                     @Valid @RequestBody PedidoRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(pedidoService.actualizar(id, request, UsuarioActual.de(auth)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','CLIENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
        pedidoService.eliminar(id, UsuarioActual.de(auth));
        return ResponseEntity.noContent().build();
    }

    /** Transiciones operativas: Diseño/Producción actualizan el pedido con el mismo JWT propagado por Feign. */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERACIONES')")
    public ResponseEntity<PedidoResponse> cambiarEstado(@PathVariable Integer id,
                                                        @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, request));
    }

    @PostMapping("/{id}/cancelacion")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','CLIENTE')")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(pedidoService.cancelar(id, UsuarioActual.de(auth)));
    }

    @PostMapping("/{id}/entrega")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS')")
    public ResponseEntity<PedidoResponse> entregar(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(pedidoService.registrarEntrega(id, UsuarioActual.de(auth)));
    }

    @PostMapping("/{id}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','CLIENTE')")
    public ResponseEntity<PagoResponse> registrarPago(@PathVariable Integer id,
                                                      @Valid @RequestBody PagoRequest request,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrar(id, request, UsuarioActual.de(auth)));
    }

    @GetMapping("/{id}/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','VENTAS','OPERACIONES','CLIENTE')")
    public ResponseEntity<List<PagoResponse>> pagos(@PathVariable Integer id, Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorPedido(id, UsuarioActual.de(auth)));
    }
}
