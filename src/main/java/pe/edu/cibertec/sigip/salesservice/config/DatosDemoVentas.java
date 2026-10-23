package pe.edu.cibertec.sigip.salesservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.sigip.salesservice.model.Cliente;
import pe.edu.cibertec.sigip.salesservice.repository.ClienteRepository;

import java.time.LocalDateTime;

/**
 * Datos de demostración: nombres ficticios con formatos peruanos válidos.
 * Se insertan solo si el documento no existe, por lo que el arranque es idempotente.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sigip.ventas.datos-demo", havingValue = "true", matchIfMissing = true)
public class DatosDemoVentas implements CommandLineRunner {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public void run(String... args) {
        cliente("RUC", "20481234566", "Comercial Norte SAC", "Andrea Torres", "944562311", "ventas@comercialnorte.demo", "Av. América Norte 1420, Trujillo");
        cliente("RUC", "20547891237", "Distribuidora El Pacífico EIRL", "Luis Mendoza", "961447852", "pedidos@elpacifico.demo", "Jr. Pizarro 845, Trujillo");
        cliente("RUC", "20614578905", "Inversiones Santa Isabel SAC", "María Paredes", "987561204", "compras@santaisabel.demo", "Av. Larco 780, Víctor Larco");
        cliente("RUC", "20596325875", "Corporación Gráfica del Norte SAC", "Jorge Reyes", "972884115", "contacto@graficanorte.demo", "Av. Nicolás de Piérola 1180, Trujillo");
        cliente("RUC", "20625874131", "Agroexportadora Moche SAC", "Rosa Vásquez", "955214783", "logistica@agromoche.demo", "Carretera Industrial Km 3.5, Moche");
        cliente("RUC", "20456987126", "Tecnología y Oficina Perú SAC", "Miguel Salazar", "966741225", "operaciones@toficina.demo", "Urb. California Mz. F Lt. 12, Trujillo");
        cliente("RUC", "20687412355", "Restaurante Sabor Norteño SAC", "Paola Chávez", "978332110", "administracion@sabornorteno.demo", "Av. España 520, Trujillo");
        cliente("RUC", "20574123691", "Clínica San Gabriel EIRL", "Diana Cabrera", "949885671", "marketing@csangabriel.demo", "Jr. Bolívar 1122, Trujillo");
        cliente("RUC", "20633214582", "Eventos y Publicidad Impacto SAC", "Fernando León", "981236547", "ventas@impactosac.demo", "Av. Fátima 1325, Víctor Larco");

        cliente("DNI", "45871236", "Carlos Ríos Calderón", "Carlos Ríos", "987654321", "carlos.rios@example.com", "Urb. San Andrés, Trujillo");
        cliente("DNI", "74125896", "María Fernanda Castillo Ruiz", "María Castillo", "955332781", "maria.castillo@example.com", "Urb. Primavera, Trujillo");
        cliente("DNI", "60321478", "José Luis Gamarra Soto", "José Gamarra", "964220517", "jose.gamarra@example.com", "Florencia de Mora, Trujillo");
        cliente("DNI", "71589632", "Rocío Alexandra Peña Vega", "Rocío Peña", "979114320", "rocio.pena@example.com", "Urb. Monserrate, Trujillo");
        cliente("DNI", "48231597", "Diego Armando Rojas Silva", "Diego Rojas", "951778430", "diego.rojas@example.com", "Jr. Junín 680, Trujillo");
        cliente("DNI", "73698514", "Valeria Soto Ramírez", "Valeria Soto", "943112870", "valeria.soto@example.com", "La Esperanza, Trujillo");
    }

    private void cliente(String tipoDocumento, String numeroDocumento, String razonSocial, String contacto,
                         String telefono, String email, String direccion) {
        if (clienteRepository.existsByTipoDocumentoIgnoreCaseAndNumeroDocumentoIgnoreCase(tipoDocumento, numeroDocumento)) return;
        clienteRepository.save(Cliente.builder()
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(numeroDocumento)
                .razonSocial(razonSocial)
                .contacto(contacto)
                .telefono(telefono)
                .email(email)
                .direccion(direccion)
                .activo(true)
                .fechaRegistro(LocalDateTime.now())
                .build());
    }
}
