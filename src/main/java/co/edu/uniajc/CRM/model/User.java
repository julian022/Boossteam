package co.edu.uniajc.CRM.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id//no es necesario el column  el  jpa asume que es la columna es la de id con el @id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nombre (máx 30)
    @Column(name = "nombre", nullable = false, length = 30)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre debe tener máximo 30 caracteres")
    private String name;

    // correo único
    @Column(name = "correo", nullable = false, length = 120, unique = true)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String email;

    // rol: ADMIN o VENDEDOR
    public enum Role { ADMIN, VENDEDOR }

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 12)
    private Role role;

    // contraseña encriptada
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    // estado
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // auditoría
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
