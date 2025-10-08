package co.edu.uniajc.crm.dto;

import co.edu.uniajc.crm.model.Proyecto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de proyecto")
public record ProyectoResponse(
        Long id, String nombre, Proyecto.Estado estado, String descripcion,
        Boolean active
) {}
