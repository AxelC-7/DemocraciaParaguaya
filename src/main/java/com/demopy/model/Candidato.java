package com.demopy.model;

import java.util.List;
import java.util.Map;

public class Candidato {
    private String id;
    private String nombre;
    private String partido;
    private List<String> noticias;
    private List<String> redesSociales;
    private String sentimientoGeneral;
    private Map<String, String> desgloseSentimiento;
    private List<Fuente> fuentes;
    private String searchEntryPointHtml;

    public Candidato() {}

    public Candidato(String id, String nombre, String partido, List<String> noticias,
                     List<String> redesSociales, String sentimientoGeneral,
                     Map<String, String> desgloseSentimiento) {
        this.id = id;
        this.nombre = nombre;
        this.partido = partido;
        this.noticias = noticias;
        this.redesSociales = redesSociales;
        this.sentimientoGeneral = sentimientoGeneral;
        this.desgloseSentimiento = desgloseSentimiento;
        this.fuentes = List.of();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPartido() { return partido; }
    public void setPartido(String partido) { this.partido = partido; }
    public List<String> getNoticias() { return noticias; }
    public void setNoticias(List<String> noticias) { this.noticias = noticias; }
    public List<String> getRedesSociales() { return redesSociales; }
    public void setRedesSociales(List<String> redesSociales) { this.redesSociales = redesSociales; }
    public String getSentimientoGeneral() { return sentimientoGeneral; }
    public void setSentimientoGeneral(String sentimientoGeneral) { this.sentimientoGeneral = sentimientoGeneral; }
    public Map<String, String> getDesgloseSentimiento() { return desgloseSentimiento; }
    public void setDesgloseSentimiento(Map<String, String> desgloseSentimiento) { this.desgloseSentimiento = desgloseSentimiento; }
    public List<Fuente> getFuentes() { return fuentes; }
    public void setFuentes(List<Fuente> fuentes) { this.fuentes = fuentes; }
    public String getSearchEntryPointHtml() { return searchEntryPointHtml; }
    public void setSearchEntryPointHtml(String searchEntryPointHtml) { this.searchEntryPointHtml = searchEntryPointHtml; }
}
