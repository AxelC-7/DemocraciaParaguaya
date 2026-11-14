package com.demopy.model;

import java.util.ArrayList;
import java.util.List;

public class Analisis {
    private String id;
    private String tipo;
    private String modelo;
    private String fechaAnalisis;
    private String resumen;
    private String sentimiento;
    private String postura;
    private Double confianza;
    private List<String> temas = new ArrayList<>();
    private List<String> afirmaciones = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private List<String> referencias = new ArrayList<>();

    public Analisis() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getFechaAnalisis() { return fechaAnalisis; }
    public void setFechaAnalisis(String fechaAnalisis) { this.fechaAnalisis = fechaAnalisis; }
    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }
    public String getSentimiento() { return sentimiento; }
    public void setSentimiento(String sentimiento) { this.sentimiento = sentimiento; }
    public String getPostura() { return postura; }
    public void setPostura(String postura) { this.postura = postura; }
    public Double getConfianza() { return confianza; }
    public void setConfianza(Double confianza) { this.confianza = confianza; }
    public List<String> getTemas() { return temas; }
    public void setTemas(List<String> temas) { this.temas = temas; }
    public List<String> getAfirmaciones() { return afirmaciones; }
    public void setAfirmaciones(List<String> afirmaciones) { this.afirmaciones = afirmaciones; }
    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
    public List<String> getReferencias() { return referencias; }
    public void setReferencias(List<String> referencias) { this.referencias = referencias; }
}
