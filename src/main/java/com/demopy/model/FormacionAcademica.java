package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class FormacionAcademica {
    private String titulo;
    private String institucion;
    private String area;
    private String fechaInicio;
    private String fechaFin;
    private List<Fuente> fuentes = new ArrayList<>();

    public FormacionAcademica() {}
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getInstitucion() { return institucion; }
    public void setInstitucion(String institucion) { this.institucion = institucion; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
