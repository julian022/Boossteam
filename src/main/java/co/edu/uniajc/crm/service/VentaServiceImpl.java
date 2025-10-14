package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.model.Venta;
import co.edu.uniajc.crm.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;

    public VentaServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta actualizarEstado(Long idVenta, Integer nuevoEstado, String usuario) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Integer estadoActual = venta.getEstado();

        // Solo se puede avanzar o marcar como perdida (0)
        if (nuevoEstado < estadoActual && nuevoEstado != 0) {
            throw new RuntimeException("No se puede volver a etapas previas");
        }

        venta.setEstado(nuevoEstado);
        venta.setUsuarioUltimaActualizacion(usuario);
        venta.setFechaActualizacion(LocalDateTime.now());

        return ventaRepository.save(venta);
    }
}

