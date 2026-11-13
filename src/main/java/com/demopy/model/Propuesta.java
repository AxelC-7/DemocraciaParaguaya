package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Propuesta {
    private String id;
    private String personaId;
    private String postulacionId;
    private String titulo;
    private String descripcion;
    private String tema;
    private String fechaRegistro;
    private String estadoVerificacion;
    private List<Fuente> fuentes = new ArrayList<>();

    public Propuesta() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public String getPostulacionId() { return postulacionId; }
    public void setPostulacionId(String postulacionId) { this.postulacionId = postulacionId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public String getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(String estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
