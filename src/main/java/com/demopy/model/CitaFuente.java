package com.demopy.model;

import java.util.List;

public class CitaFuente {
    private String texto;
    private List<Fuente> fuentes;

    public CitaFuente() {}

    public CitaFuente(String texto, List<Fuente> fuentes) {
        this.texto = texto;
        this.fuentes = fuentes;
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
}
