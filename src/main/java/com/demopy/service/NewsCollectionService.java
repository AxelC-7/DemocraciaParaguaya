package com.demopy.service;

import com.demopy.model.Noticia;
import com.demopy.model.ResultadoBusqueda;
import com.demopy.scraper.GenericWebScraper;
import com.demopy.storage.JsonFileStorageService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import com.demopy.model.Fuente;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsCollectionService {
    private final DuckDuckGoSearchService searchService;
    private final GenericWebScraper scraper;
    private final ContenidoDataService contenidoService;
    private final JsonFileStorageService storage;

    public NewsCollectionService(DuckDuckGoSearchService searchService,
                                 GenericWebScraper scraper,
                                 ContenidoDataService contenidoService,
                                 JsonFileStorageService storage) {
        this.searchService = searchService;
        this.scraper = scraper;
        this.contenidoService = contenidoService;
        this.storage = storage;
    }

    public List<Noticia> buscarYRecolectar(String consulta, int limite) throws IOException {
        List<ResultadoBusqueda> resultados = searchService.buscar(consulta, limite);
        Map<String, Noticia> recolectadas = new LinkedHashMap<>();
        List<String> errores = new ArrayList<>();

        for (ResultadoBusqueda resultado : resultados) {
            try {
                Noticia noticia = scraper.extraer(resultado.getUrl());
                if (noticia.getTitulo() == null || noticia.getTitulo().isBlank()) {
                    noticia.setTitulo(resultado.getTitulo());
                }
                if ((noticia.getResumen() == null || noticia.getResumen().isBlank())
                        && resultado.getFragmento() != null) {
                    noticia.setResumen(resultado.getFragmento());
                }
                noticia.setId(hash(noticia.getUrl()));
                noticia.setHashContenido(hash(noticia.getContenido() == null ? "" : noticia.getContenido()));
                List<Fuente> fuentes = new ArrayList<>();
                if (noticia.getFuentes() != null) fuentes.addAll(noticia.getFuentes());
                fuentes.add(new Fuente(resultado.getTitulo(), resultado.getUrl()));
                noticia.setFuentes(fuentes);
                contenidoService.guardarNoticia(noticia);
                recolectadas.putIfAbsent(noticia.getId(), noticia);
            } catch (Exception ex) {
                errores.add(resultado.getUrl() + " -> " + ex.getClass().getSimpleName());
            }
        }

        storage.guardar("noticias/ultima-recoleccion.json", Map.of(
                "consulta", consulta,
                "resultadosEncontrados", resultados.size(),
                "noticiasGuardadas", recolectadas.size(),
                "errores", errores,
                "fecha", java.time.Instant.now().toString()
        ));
        return new ArrayList<>(recolectadas.values());
    }

    private String hash(String valor) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible.", ex);
        }
    }
}
