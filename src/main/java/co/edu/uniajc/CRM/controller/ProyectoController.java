package co.edu.uniajc.CRM.controller;

import co.edu.uniajc.CRM.model.Proyecto;
import co.edu.uniajc.CRM.service.ProyectoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping
    public List<Proyecto> all() {
        return proyectoService.getAllProyectos();
    }

    @GetMapping("/{id}")
    public Proyecto byId(@PathVariable Long id) {
        return proyectoService.getProyectoById(id);
    }

    @PostMapping
    public Proyecto create(@RequestBody Proyecto proyecto) {
        return proyectoService.saveProyecto(proyecto);
    }

    @PutMapping("/{id}")
    public Proyecto update(@PathVariable Long id, @RequestBody Proyecto proyecto) {
        return proyectoService.updateProyecto(id, proyecto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        proyectoService.deleteProyecto(id);
    }

    // --- Reporte: cuántos proyectos hay en cada estado ---
    @GetMapping("/reporte/por-estado")
    public Map<String, Long> reportePorEstado() {
        return proyectoService.contarPorEstado();
    }
}
