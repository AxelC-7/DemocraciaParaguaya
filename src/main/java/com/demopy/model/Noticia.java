package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Noticia {
    private String id;
    private String titulo;
    private String resumen;
    private String contenido;
    private String url;
    private String medio;
    private String autor;
    private String fechaPublicacion;
    private String fechaRecoleccion;
    private String hashContenido;
    private List<String> personasRelacionadas = new ArrayList<>();
    private List<String> temas = new ArrayList<>();
    private List<Fuente> fuentes = new ArrayList<>();

    public Noticia() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMedio() { return medio; }
    public void setMedio(String medio) { this.medio = medio; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getFechaRecoleccion() { return fechaRecoleccion; }
    public void setFechaRecoleccion(String fechaRecoleccion) { this.fechaRecoleccion = fechaRecoleccion; }
    public String getHashContenido() { return hashContenido; }
    public void setHashContenido(String hashContenido) { this.hashContenido = hashContenido; }
    public List<String> getPersonasRelacionadas() { return personasRelacionadas; }
    public void setPersonasRelacionadas(List<String> personasRelacionadas) { this.personasRelacionadas = personasRelacionadas; }
    public List<String> getTemas() { return temas; }
    public void setTemas(List<String> temas) { this.temas = temas; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
