package co.edu.uniajc.crm.service;

import co.edu.uniajc.crm.dto.ProyectoRequest;
import co.edu.uniajc.crm.dto.ProyectoResponse;
import co.edu.uniajc.crm.exception.NotFoundException;
import co.edu.uniajc.crm.model.Proyecto;
import co.edu.uniajc.crm.repository.ProyectoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        // opcional pero útil si quieres el mismo SQL-mode que tu app
        "spring.datasource.url=jdbc:h2:mem:crm-test;DB_CLOSE_DELAY=-1;MODE=MySQL"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EntityScan(basePackages = "co.edu.uniajc.crm.model")
@EnableJpaRepositories(basePackages = "co.edu.uniajc.crm.repository")
@Import(ProyectoService.class)
@ActiveProfiles("test")
class ProyectoServiceTest {

    @Autowired ProyectoRepository repo;
    @Autowired ProyectoService service;

    @BeforeEach void clean(){ repo.deleteAll(); }

    @Test
    void crear_ok() {
        var req = new ProyectoRequest("CRM", Proyecto.Estado.BACKLOG, "desc"); // helper para enum/string
        ProyectoResponse out = service.crear(req);
        assertNotNull(out.id());
        assertEquals("CRM", out.nombre());
        assertTrue(out.estado().toString().equalsIgnoreCase("BACKLOG") || out.estado().toString().equalsIgnoreCase("backlog"));
    }

    @Test
    void actualizar_ok() {
        var p = repo.save(new Proyecto(null,"A",Proyecto.Estado.BACKLOG,"d", true, null, null));
        var req = new ProyectoRequest("A2", Proyecto.Estado.DOING, "nueva");

        ProyectoResponse out = service.actualizar(p.getId(), req);
        assertEquals("A2", out.nombre());
        assertTrue(out.estado().toString().equalsIgnoreCase("DOING"));
    }

    @Test
    void eliminar_logico_ok() {
        var p = repo.save(new Proyecto(null,"A",Proyecto.Estado.BACKLOG,"d", true, null, null));
        service.eliminar(p.getId());
        assertFalse(repo.findById(p.getId()).orElseThrow().getActive());
    }

    @Test
    void buscar_404() {
        assertThrows(NotFoundException.class, () -> service.buscarPorId(999L));
    }

    // -------- helpers --------
    /** Permite construir ProyectoRequest tanto si 'estado' es enum (tu DTO lo tipa como enum) o String */
    private Object enumOrString(String val) {
        try {
            // Si tu ProyectoRequest espera enum co.edu.uniajc.crm.model.Proyecto.Estado:
            var enumClass = Class.forName("co.edu.uniajc.crm.model.Proyecto$Estado");
            @SuppressWarnings("unchecked") var e = Enum.valueOf((Class<Enum>) enumClass, val);
            return e;
        } catch (Exception ignore) {
            // Si no existe el enum o el DTO espera String
            return val.toLowerCase();
        }
    }
}
