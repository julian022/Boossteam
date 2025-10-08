package co.edu.uniajc.crm.controller;

import co.edu.uniajc.crm.dto.*;
import co.edu.uniajc.crm.model.Proyecto;
import co.edu.uniajc.crm.repository.ProyectoRepository;
import co.edu.uniajc.crm.service.ProyectoService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos")
public class ProyectoController {

    private final ProyectoService service;
    private final ProyectoRepository repo;

    @Operation(summary = "Crear proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creación exitosa",
                    content = @Content(schema = @Schema(implementation = ApiProyectoEnvelope.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiValidationError.class)))
    })
    @PostMapping(path = "/crearproyecto", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiProyectoEnvelope> crear(@Valid @RequestBody ProyectoRequest req) {
        var resp = service.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiProyectoEnvelope("Creación de proyecto exitosa", resp));
    }

    @Operation(summary = "Listar proyectos")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProyectoResponse.class))))
    @GetMapping(value = "/listarproyectos", produces = "application/json")
    public ResponseEntity<List<ProyectoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(summary = "Buscar por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ProyectoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado",
                    content = @Content(schema = @Schema(implementation = ApiMessage.class)))
    })
    @GetMapping(path = "/buscarID/{id}", produces = "application/json")
    public ResponseEntity<ProyectoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Actualizar por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualización exitosa",
                    content = @Content(schema = @Schema(implementation = ApiProyectoEnvelope.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado",
                    content = @Content(schema = @Schema(implementation = ApiMessage.class)))
    })
    @PutMapping(path = "/actProyectoPorID/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiProyectoEnvelope> actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoRequest req) {
        var resp = service.actualizar(id, req);
        return ResponseEntity.ok(new ApiProyectoEnvelope("Actualización de proyecto exitosa", resp));
    }

    @Operation(summary = "Eliminar (inactivar) por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inactivado",
                    content = @Content(schema = @Schema(implementation = ApiMessage.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado",
                    content = @Content(schema = @Schema(implementation = ApiMessage.class)))
    })
    @DeleteMapping(path = "/delProyecto/{id}", produces = "application/json")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok(Map.of("message", "Proyecto eliminado (inactivado)"));
    }

    // --- Reporte: cuántos proyectos hay en cada estado ---
    public Map<String, Long> contarPorEstado() {
        var out = new java.util.LinkedHashMap<String, Long>();
        for (var e : Proyecto.Estado.values()) {
            out.put(e.name(), repo.countByEstado(e)); // claves "BACKLOG","DOING","DONE"
        }
        return out;
    }
}



