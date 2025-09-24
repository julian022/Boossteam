package co.edu.uniajc.CRM.repository;

import co.edu.uniajc.CRM.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional<User> findByCorreo(String correo);
}
