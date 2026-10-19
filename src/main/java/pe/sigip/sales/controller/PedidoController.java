package pe.sigip.sales.controller;
import pe.sigip.sales.model.Pedido;
import pe.sigip.sales.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/pedidos") @CrossOrigin(origins="http://localhost:4200")
public class PedidoController {
 private final PedidoRepository repo;
 public PedidoController(PedidoRepository repo){this.repo=repo;}
 @GetMapping public List<Pedido> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Pedido buscar(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Pedido no encontrado"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Pedido crear(@Valid @RequestBody Pedido p){p.setId(null); if(p.getEstado()==null||p.getEstado().isBlank())p.setEstado("PENDIENTE_PAGO"); return repo.save(p);}
 @PutMapping("/{id}") public Pedido actualizar(@PathVariable Long id,@Valid @RequestBody Pedido datos){Pedido p=buscar(id);p.setDescripcion(datos.getDescripcion());p.setTotal(datos.getTotal());p.setEstado(datos.getEstado());return repo.save(p);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable Long id){if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND);repo.deleteById(id);}
 @GetMapping("/health") public Map<String,String> health(){return Map.of("servicio","SIGIP Sales Service","estado","activo");}
}