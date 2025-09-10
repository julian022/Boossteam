package co.edu.uniajc.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con mensaje y un usuario")
public record ApiUserEnvelope(
        @Schema(example = "Creación de usuario exitosa") String message,
        UserResponse user
) {}
