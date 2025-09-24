package co.edu.uniajc.CRM.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "proyectos")
@Data
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String estado;
    private Double monto;

    private LocalDate fechaCreacion;
    private LocalDate ultimaActualizacion;
}
