package co.edu.uniajc.CRM.controller;

import co.edu.uniajc.CRM.dto.*;
import co.edu.uniajc.CRM.dto.UserUpdateRequest;
import co.edu.uniajc.CRM.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin // si lo usarás con front Angular
public class UserController {

    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @PostMapping(path="/crearusuario")
    public ResponseEntity<?> create(@Valid @RequestBody UserRequest req) {
        var created = service.create(req);
        // Mensaje de éxito solicitado
        return ResponseEntity.ok(Map.of(
                "message", "Creación de usuario exitosa",
                "user", created
        ));
    }

    @GetMapping(path="/listarusuarios")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(path="/buscarID/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping(path="/actUserporID/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody UserUpdateRequest req) {
        var updated = service.update(id, req);
        return ResponseEntity.ok(Map.of(
                "message", "Actualización de usuario exitosa",
                "user", updated
        ));
    }

    @DeleteMapping("/delUser/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado (inactivado)"));
    }
}
