package co.edu.uniajc.CRM.controller;

import co.edu.uniajc.CRM.model.User;
import co.edu.uniajc.CRM.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

// Usamos @DataJpaTest + Import del stack mínimo para MockMvc sin levantar toda la app.
// Alternativamente se puede usar @SpringBootTest
//@DataJpaTest//@Import({UserController.class, UserService.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    UserRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }


    @Test
    void post_create_ok() throws Exception {
        var json = """
                {
                  "name":"Carlos",
                  "email":"carlos@test.com",
                  "role":"ADMIN",
                  "password":"Secreta123"
                }
                """;

        var result = mvc.perform(post("/api/users/crearusuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 201, "HTTP esperado 200/201 pero fue " + status);

        var body = result.getResponse().getContentAsString();
        var root = om.readTree(body);

        // Soporta { "user": {...}, "message": "..." } O directamente {id,name,email,...}
        var userNode = root.has("user") ? root.get("user") : root;

        assertNotNull(userNode.get("id"), "Debe venir id");
        assertEquals("carlos@test.com", userNode.get("email").asText());

        if (root.has("message")) {
            assertTrue(root.get("message").asText().toLowerCase().contains("creación")
                            || root.get("message").asText().toLowerCase().contains("creacion"),
                    "Mensaje debería indicar creación exitosa, body=" + body);
        }
    }

    @Test
    void post_create_conflict_email() throws Exception {
        repo.save(User.builder()
                .name("Existente")
                .email("dup@test.com")
                .role(User.Role.ADMIN)
                .passwordHash("x").build());

        var json = """
                {"name":"Nuevo","email":"dup@test.com","role":"VENDEDOR","password":"Secreta123"}
                """;

        var result = mvc.perform(post("/api/users/crearusuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andReturn();

        int status = result.getResponse().getStatus();
        // Aceptamos 409 (ideal) o 400 si tu handler aún mapea validación/conflicto como bad request
        assertTrue(status == 409 || status == 400,
                "HTTP esperado 409/400 por conflicto, fue " + status);

        var body = result.getResponse().getContentAsString();
        if (!body.isBlank()) {
            var root = om.readTree(body);
            if (root.has("message")) {
                var msg = root.get("message").asText().toLowerCase();
                assertTrue(msg.contains("correo") || msg.contains("asignado") || msg.contains("ya existe"),
                        "Mensaje no parece de conflicto por correo. body=" + body);
            }
        }
    }

    @Test
    void put_update_ok() throws Exception {
        var u = repo.save(User.builder()
                .name("Viejo")
                .email("user@test.com")
                .role(User.Role.VENDEDOR)
                .passwordHash("x").build());

        var json = """
                {"name":"Nuevo","role":"ADMIN","password":"NuevaClave1"}
                """;

        var result = mvc.perform(put("/api/users/actUserporID/" + u.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());

        var root = om.readTree(result.getResponse().getContentAsString());
        var userNode = root.has("user") ? root.get("user") : root;

        assertEquals("Nuevo", userNode.get("name").asText());
        assertEquals("ADMIN", userNode.get("role").asText());
        if (root.has("message")) {
            assertTrue(root.get("message").asText().toLowerCase().contains("actualiz"),
                    "Mensaje debería indicar actualización exitosa");
        }
    }

    @Test
    void delete_inactivate_ok() throws Exception {
        var u = repo.save(User.builder()
                .name("Borrar")
                .email("borrar@test.com")
                .role(User.Role.VENDEDOR)
                .passwordHash("x")
                .active(true).build());

        var result = mvc.perform(delete("/api/users/delUser/" + u.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        var body = result.getResponse().getContentAsString();
        if (!body.isBlank()) {
            var root = om.readTree(body);
            if (root.has("message")) {
                var msg = root.get("message").asText().toLowerCase();
                assertTrue(msg.contains("inactiv") || msg.contains("elimin"),
                        "Mensaje debería indicar inactivación/eliminación. body=" + body);
            }
        }

        // Verifica que realmente quedó inactivo en la BD
        var entity = repo.findById(u.getId()).orElseThrow();
        assertFalse(entity.getActive(), "El usuario debería quedar inactivo");
    }
}