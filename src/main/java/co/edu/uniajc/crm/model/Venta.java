package co.edu.uniajc.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreProspecto; // Nombre del contacto o empresa asociada

    private Integer estado; // 1=Pre-oferta, 2=Oferta, etc.

    private String usuarioUltimaActualizacion;

    private LocalDateTime fechaActualizacion;

    public Venta() {
        this.estado = 1; // Estado inicial por defecto: Pre-oferta
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreProspecto() { return nombreProspecto; }
    public void setNombreProspecto(String nombreProspecto) { this.nombreProspecto = nombreProspecto; }

    public Integer getEstado() { return estado; }
    public void setEstado(Integer estado) { this.estado = estado; }

    public String getUsuarioUltimaActualizacion() { return usuarioUltimaActualizacion; }
    public void setUsuarioUltimaActualizacion(String usuarioUltimaActualizacion) {
        this.usuarioUltimaActualizacion = usuarioUltimaActualizacion;
    }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
