package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.dto.*;
import co.edu.uniajc.crm.exception.ConflictException;
import co.edu.uniajc.crm.exception.NotFoundException;
import co.edu.uniajc.crm.mapper.UserMapper;
import co.edu.uniajc.crm.dto.UserUpdateRequest;
import co.edu.uniajc.crm.model.User;
import co.edu.uniajc.crm.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserResponse create(UserRequest req) {
        if (repo.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("Este correo se encuentra asignado a otro usuario, por favor intente con otro");
        }

        User user = User.builder()
                .name(req.name().trim())
                .email(req.email().trim().toLowerCase())
                .role(req.role())
                .passwordHash(passwordEncoder.encode(req.password()))
                .active(true)
                .build();

        return UserMapper.toResponse(repo.save(user));
    }
    String msg = "Usuario no encontrado";

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return repo.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(msg));
        return UserMapper.toResponse(u);
    }

    public UserResponse update(Long id, UserUpdateRequest req) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(msg));

        u.setName(req.name().trim());
        u.setRole(req.role());

        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
        }

        return UserMapper.toResponse(u); // JPA flush en @Transactional
    }

    /** Borrado lógico (activo = false) */
    public void delete(Long id) {
        User u = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(msg));
        u.setActive(false);
    }
}
