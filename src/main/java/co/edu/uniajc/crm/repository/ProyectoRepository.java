package co.edu.uniajc.crm.repository;
import co.edu.uniajc.crm.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    long countByEstado(Proyecto.Estado estado);
}

