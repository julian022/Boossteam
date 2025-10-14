package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.model.Venta;
import java.util.List;

public interface VentaService {
    List<Venta> listarVentas();
    Venta actualizarEstado(Long idVenta, Integer nuevoEstado, String usuario);
}
