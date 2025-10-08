package co.edu.uniajc.crm.controller;

import co.edu.uniajc.crm.model.Proyecto;
import co.edu.uniajc.crm.repository.ProyectoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mismo patrón que los tests de Usuario:
 * - Arranca contexto completo (SpringBootTest)
 * - Perfil test activo (H2, Flyway OFF) vía @ActiveProfiles("test")
 * - MockMvc para probar endpoints reales
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProyectoControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ProyectoRepository repo;

    @BeforeEach void clean() { repo.deleteAll(); }

    private JsonNode unwrapProyecto(String body) throws Exception {
        var root = om.readTree(body);
        return root.has("proyecto") ? root.get("proyecto") : root; // tolera envoltorio {message, proyecto} o objeto directo
    }

    @Test
    void post_crear_201_ok() throws Exception {
        var json = """
      {"nombre":"CRM Boossteam","estado":"BACKLOG","descripcion":"primera versión"}
    """;

        var res = mvc.perform(post("/api/proyectos/crearproyecto")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        var prj = unwrapProyecto(res.getResponse().getContentAsString());
        assertNotNull(prj.get("id"));
        assertEquals("CRM Boossteam", prj.get("nombre").asText());
        assertTrue(prj.get("estado").asText().equalsIgnoreCase("BACKLOG"));
    }

    @Test
    void post_crear_400_validacion() throws Exception {
        var json = """
    {"nombre":"","estado":"","descripcion":""}
  """;
        mvc.perform(post("/api/proyectos/crearproyecto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
        // Si tu handler devuelve { "message": "...", "errors": {...} } puedes añadir:
        // .andExpect(jsonPath("$.message").exists())
        // .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void get_listar_200_ok() throws Exception {
        // Usa builder o setters para evitar constructor frágil
        repo.save(Proyecto.builder()
                .nombre("A").estado(Proyecto.Estado.BACKLOG).descripcion("d1").active(true).build());
        repo.save(Proyecto.builder()
                .nombre("B").estado(Proyecto.Estado.DOING).descripcion("d2").active(true).build());

        mvc.perform(get("/api/proyectos/listarproyectos").accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void get_buscar_200_ok_y_404() throws Exception {
        var p = repo.save(Proyecto.builder()
                .nombre("A").estado(Proyecto.Estado.BACKLOG).descripcion("d").active(true).build());

        mvc.perform(get("/api/proyectos/buscarID/" + p.getId()).accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()));

        mvc.perform(get("/api/proyectos/buscarID/999999").accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void put_actualizar_200_ok_y_404() throws Exception {
        var p = repo.save(Proyecto.builder()
                .nombre("A").estado(Proyecto.Estado.BACKLOG).descripcion("d").active(true).build());

        var json = """
      {"nombre":"A2","estado":"DONE","descripcion":"final"}
    """;

        var res = mvc.perform(put("/api/proyectos/actProyectoPorID/" + p.getId())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        var prj = unwrapProyecto(res.getResponse().getContentAsString());
        assertEquals("A2", prj.get("nombre").asText());
        assertTrue(prj.get("estado").asText().equalsIgnoreCase("DONE"));

        mvc.perform(put("/api/proyectos/actProyectoPorID/999999")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_eliminar_200_ok_y_404() throws Exception {
        var p = repo.save(Proyecto.builder()
                .nombre("A").estado(Proyecto.Estado.BACKLOG).descripcion("d").active(true).build());

        mvc.perform(delete("/api/proyectos/delProyecto/" + p.getId()).accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // borrado lógico (igual que User)
        assertFalse(repo.findById(p.getId()).orElseThrow().getActive());

        mvc.perform(delete("/api/proyectos/delProyecto/999999").accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
