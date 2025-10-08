package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.dto.ProyectoRequest;
import co.edu.uniajc.crm.dto.ProyectoResponse;
import co.edu.uniajc.crm.exception.NotFoundException;
import co.edu.uniajc.crm.mapper.ProyectoMapper;
import co.edu.uniajc.crm.model.Proyecto;
import co.edu.uniajc.crm.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository repo;

    public ProyectoResponse crear(ProyectoRequest req) {
        var p = ProyectoMapper.toEntity(req);
        return ProyectoMapper.toResponse(repo.save(p));
    }

    public ProyectoResponse buscarPorId(Long id) {
        var p = repo.findById(id).orElseThrow(() -> new NotFoundException("Proyecto no encontrado"));
        return ProyectoMapper.toResponse(p);
    }

    public List<ProyectoResponse> listar() {
        return repo.findAll().stream().map(ProyectoMapper::toResponse).toList();
    }

    public ProyectoResponse actualizar(Long id, ProyectoRequest req) {
        var p = repo.findById(id).orElseThrow(() -> new NotFoundException("Proyecto no encontrado"));
        ProyectoMapper.update(p, req);
        return ProyectoMapper.toResponse(repo.save(p));
    }

    public void eliminar(Long id) {
        var p = repo.findById(id).orElseThrow(() -> new NotFoundException("Proyecto no encontrado"));
        p.setActive(false);
        repo.save(p);
    }
}
