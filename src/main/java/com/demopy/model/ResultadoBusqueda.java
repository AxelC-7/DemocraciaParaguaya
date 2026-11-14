package com.demopy.model;

public class ResultadoBusqueda {
    private String titulo;
    private String url;
    private String fragmento;
    private String buscador;

    public ResultadoBusqueda() {}
    public ResultadoBusqueda(String titulo, String url, String fragmento, String buscador) {
        this.titulo = titulo;
        this.url = url;
        this.fragmento = fragmento;
        this.buscador = buscador;
    }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFragmento() { return fragmento; }
    public void setFragmento(String fragmento) { this.fragmento = fragmento; }
    public String getBuscador() { return buscador; }
    public void setBuscador(String buscador) { this.buscador = buscador; }
}
