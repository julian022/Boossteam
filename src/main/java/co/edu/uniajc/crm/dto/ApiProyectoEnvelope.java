package co.edu.uniajc.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con mensaje y un proyecto")
public record ApiProyectoEnvelope(
        @Schema(example = "Creación de proyecto exitosa") String message,
        ProyectoResponse proyecto
) {}
