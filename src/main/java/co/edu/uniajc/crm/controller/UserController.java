package co.edu.uniajc.crm.controller;

import co.edu.uniajc.crm.dto.*;
import co.edu.uniajc.crm.dto.UserUpdateRequest;
import co.edu.uniajc.crm.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

import java.util.Map;

@Tag(name = "Usuarios", description = "HU5 - Gestión de usuarios")
@RestController
@RequestMapping("/api/users")
@CrossOrigin // si lo usarás con front Angular
public class UserController {



    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @Operation(summary = "Crear usuario", description = "Valida correo único y contraseña (8–16).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creación exitosa",
                    content = @Content(mediaType="application/json",
                            schema = @Schema(implementation = ApiUserEnvelope.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiValidationError.class))),
            @ApiResponse(responseCode = "409", description = "Correo ya asignado",
                    content = @Content(schema = @Schema(implementation = ApiMessage.class)))
    })


    @PostMapping(path="/crearusuario", consumes="application/json", produces="application/json")
    public ResponseEntity<ApiUserEnvelope> create(@Valid @RequestBody UserRequest req) {
        var created = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiUserEnvelope("Creación de usuario exitosa", created));
    }

    @GetMapping(path="/listarusuarios",consumes="application/json")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(path="/buscarID/{id}",consumes="application/json")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping(path="/actUserporID/{id}", produces="application/json")
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
