package co.edu.uniajc.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensaje simple")
public record ApiMessage(
        @Schema(example = "Usuario no encontrado") String message
) {}
 