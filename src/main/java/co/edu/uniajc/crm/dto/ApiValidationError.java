package co.edu.uniajc.crm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Error de validación")
public record ApiValidationError(
        @Schema(example = "Datos inválidos") String message,
        Map<String, String> errors
) {}
