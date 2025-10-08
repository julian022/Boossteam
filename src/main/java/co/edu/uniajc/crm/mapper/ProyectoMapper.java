package co.edu.uniajc.crm.mapper;

import co.edu.uniajc.crm.dto.ProyectoRequest;
import co.edu.uniajc.crm.dto.ProyectoResponse;
import co.edu.uniajc.crm.model.Proyecto;

public class ProyectoMapper {

    public static Proyecto toEntity(ProyectoRequest req) {
        return Proyecto.builder()
                .nombre(req.nombre())
                .estado(req.estado())
                .descripcion(req.descripcion())
                .build();
    }

    public static void update(Proyecto p, ProyectoRequest req) {
        if (req.nombre() != null) p.setNombre(req.nombre());
        if (req.estado() != null) p.setEstado(req.estado());
        if (req.descripcion() != null) p.setDescripcion(req.descripcion());
        p.setUpdatedAt(java.time.LocalDateTime.now());
    }

    public static ProyectoResponse toResponse(Proyecto p) {
        return new ProyectoResponse(
                p.getId(), p.getNombre(), p.getEstado(), p.getDescripcion(), p.getActive()
        );
    }
}
