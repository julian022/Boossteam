package co.edu.uniajc.CRM.repository;

import co.edu.uniajc.CRM.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    // Aquí puedes agregar consultas personalizadas si lo necesitas
    // Ejemplo: List<Proyecto> findByEstado(String estado);
}
