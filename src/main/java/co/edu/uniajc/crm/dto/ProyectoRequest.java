package co.edu.uniajc.crm.dto;

import co.edu.uniajc.crm.model.Proyecto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de creación/actualización de proyecto")
public record ProyectoRequest(
        @NotBlank @Size(max = 40) String nombre,
        @NotNull Proyecto.Estado estado,
        @Size(max = 200) String descripcion
) {}
