package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Postulacion {
    private String id;
    private String personaId;
    private String cargo;
    private String institucion;
    private String tipoSeleccion;
    private String estado;
    private String partido;
    private String circunscripcion;
    private String fechaPostulacion;
    private String fechaResolucion;
    private List<Fuente> fuentes = new ArrayList<>();

    public Postulacion() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    public String getTipoSeleccion() { return tipoSeleccion; }
    public void setTipoSeleccion(String tipoSeleccion) { this.tipoSeleccion = tipoSeleccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getPartido() { return partido; }
    public void setPartido(String partido) { this.partido = partido; }
    public String getCircunscripcion() { return circunscripcion; }
    public void setCircunscripcion(String circunscripcion) { this.circunscripcion = circunscripcion; }
    public String getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(String fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
    public String getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(String fechaResolucion) { this.fechaResolucion = fechaResolucion; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
