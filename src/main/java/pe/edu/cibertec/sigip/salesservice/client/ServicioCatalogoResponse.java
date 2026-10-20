package pe.edu.cibertec.sigip.salesservice.client;
import java.math.BigDecimal;
public record ServicioCatalogoResponse(Integer idservicio,String nombre,String descripcion,BigDecimal precioBase,Boolean requiereCotizacion,Boolean activo,Integer idcategoria,String categoria){}