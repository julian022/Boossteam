package co.edu.uniajc.CRM.service;

import co.edu.uniajc.CRM.dto.UserRequest;
import co.edu.uniajc.CRM.dto.UserUpdateRequest;
import co.edu.uniajc.CRM.exception.ConflictException;
import co.edu.uniajc.CRM.exception.NotFoundException;
import co.edu.uniajc.CRM.model.User;
import co.edu.uniajc.CRM.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserService.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // fuerza embebida
class UserServiceTest {

    @Autowired
    private UserRepository repo;

    @Autowired
    private UserService service;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void create_user_ok() {
        var req = new UserRequest("Carlos", "carlos@test.com", User.Role.ADMIN, "Secreta123");
        var resp = service.create(req);

        assertNotNull(resp.id());
        assertEquals("Carlos", resp.name());
        assertEquals("carlos@test.com", resp.email());
        assertEquals(User.Role.ADMIN, resp.role());
        assertTrue(resp.active());
        // Verifico que el hash se haya persistido
        var entity = repo.findById(resp.id()).orElseThrow();
        assertNotEquals("Secreta123", entity.getPasswordHash());
        assertTrue(entity.getPasswordHash().startsWith("$2")); // BCrypt
    }

    @Test
    void create_conflict_email() {
        repo.save(User.builder()
                .name("Otro")
                .email("dup@test.com")
                .role(User.Role.VENDEDOR)
                .passwordHash("x")
                .build());

        var req = new UserRequest("Carlos", "dup@test.com", User.Role.ADMIN, "Secreta123");
        assertThrows(ConflictException.class, () -> service.create(req));
    }

    @Test
    void findById_not_found() {
        assertThrows(NotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void update_changes_name_role_and_password_when_present() {
        var saved = repo.save(User.builder()
                .name("Viejo")
                .email("user@test.com")
                .role(User.Role.VENDEDOR)
                .passwordHash("hash")
                .build());

        var req = new UserUpdateRequest("Nuevo", User.Role.ADMIN, "NuevaClave1");
        var resp = service.update(saved.getId(), req);

        assertEquals("Nuevo", resp.name());
        assertEquals(User.Role.ADMIN, resp.role());

        var entity = repo.findById(saved.getId()).orElseThrow();
        assertNotEquals("hash", entity.getPasswordHash());
    }

    @Test
    void delete_sets_inactive() {
        var saved = repo.save(User.builder()
                .name("ToDelete")
                .email("delete@test.com")
                .role(User.Role.VENDEDOR)
                .passwordHash("hash")
                .active(true)
                .build());

        service.delete(saved.getId());

        var entity = repo.findById(saved.getId()).orElseThrow();
        assertFalse(entity.getActive());
    }
}
