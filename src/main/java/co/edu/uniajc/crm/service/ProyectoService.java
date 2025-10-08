package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.model.Proyecto;
import co.edu.uniajc.crm.repository.ProyectoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    // CRUD
    public List<Proyecto> getAllProyectos() {
        return proyectoRepository.findAll();
    }

    public Proyecto saveProyecto(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto getProyectoById(Long id) {
        return proyectoRepository.findById(id).orElse(null);
    }

    public Proyecto updateProyecto(Long id, Proyecto proyectoDetails) {
        return proyectoRepository.findById(id)
                .map(proyecto -> {
                    proyecto.setNombre(proyectoDetails.getNombre());
                    proyecto.setEstado(proyectoDetails.getEstado());
                    proyecto.setMonto(proyectoDetails.getMonto());
                    proyecto.setFechaCreacion(proyectoDetails.getFechaCreacion());
                    proyecto.setUltimaActualizacion(proyectoDetails.getUltimaActualizacion());
                    return proyectoRepository.save(proyecto);
                })
                .orElse(null);
    }

    public void deleteProyecto(Long id) {
        proyectoRepository.deleteById(id);
    }

    // Reporte simple: conteo por estado
    public Map<String, Long> contarPorEstado() {
        return proyectoRepository.findAll().stream()
                .collect(Collectors.groupingBy(Proyecto::getEstado, Collectors.counting()));
    }
}

