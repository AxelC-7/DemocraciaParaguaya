package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Testimonio {
    private String id;
    private String personaId;
    private String autor;
    private String relacionConLaPersona;
    private String contenido;
    private String fecha;
    private String consentimiento;
    private String estadoVerificacion;
    private String observaciones;
    private List<Fuente> fuentes = new ArrayList<>();

    public Testimonio() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPersonaId() { return personaId; }
    public void setPersonaId(String personaId) { this.personaId = personaId; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getRelacionConLaPersona() { return relacionConLaPersona; }
    public void setRelacionConLaPersona(String relacionConLaPersona) { this.relacionConLaPersona = relacionConLaPersona; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getConsentimiento() { return consentimiento; }
    public void setConsentimiento(String consentimiento) { this.consentimiento = consentimiento; }
    public String getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(String estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
