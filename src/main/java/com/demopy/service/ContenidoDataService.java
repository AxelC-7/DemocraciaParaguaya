package com.demopy.service;

import com.demopy.model.Analisis;
import com.demopy.model.Noticia;
import com.demopy.storage.JsonFileStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ContenidoDataService {
    private static final Pattern ID_VALIDO = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private final JsonFileStorageService storage;

    public ContenidoDataService(JsonFileStorageService storage) {
        this.storage = storage;
    }

    public void guardarNoticia(Noticia noticia) throws IOException {
        if (noticia == null) throw new IllegalArgumentException("La noticia es obligatoria.");
        validarId(noticia.getId());
        if (noticia.getPersonasRelacionadas() != null) {
            noticia.getPersonasRelacionadas().forEach(this::validarId);
        }
        storage.guardar("noticias/" + noticia.getId() + ".json", noticia);
        actualizarIndiceNoticias(noticia);
    }

    public List<Noticia> listarNoticias() throws IOException {
        String indice = "noticias/indice.json";
        if (!storage.existe(indice)) return new ArrayList<>();
        return storage.leer(indice, new TypeReference<List<Noticia>>() {});
    }

    private void actualizarIndiceNoticias(Noticia noticia) throws IOException {
        List<Noticia> noticias = new ArrayList<>(listarNoticias());
        noticias.removeIf(actual -> noticia.getId().equals(actual.getId()));
        noticias.add(noticia);
        storage.guardar("noticias/indice.json", noticias);
    }

    public Noticia obtenerNoticia(String noticiaId) throws IOException {
        validarId(noticiaId);
        return storage.leer("noticias/" + noticiaId + ".json", Noticia.class);
    }

    public void guardarAnalisis(String personaId, Analisis analisis) throws IOException {
        validarId(personaId);
        if (analisis == null) throw new IllegalArgumentException("El análisis es obligatorio.");
        validarId(analisis.getId());
        storage.guardar("personas/" + personaId + "/analisis/" + analisis.getId() + ".json", analisis);
    }

    public Analisis obtenerAnalisis(String personaId, String analisisId) throws IOException {
        validarId(personaId);
        validarId(analisisId);
        return storage.leer("personas/" + personaId + "/analisis/" + analisisId + ".json", Analisis.class);
    }

    public List<Analisis> listarAnalisis(String personaId) throws IOException {
        validarId(personaId);
        String ruta = "personas/" + personaId + "/analisis/indice.json";
        if (!storage.existe(ruta)) return new ArrayList<>();
        return storage.leer(ruta, new TypeReference<List<Analisis>>() {});
    }

    public void guardarAnalisisEIndice(String personaId, Analisis analisis) throws IOException {
        guardarAnalisis(personaId, analisis);
        List<Analisis> indice = listarAnalisis(personaId);
        indice.removeIf(item -> analisis.getId().equals(item.getId()));
        indice.add(analisis);
        storage.guardar("personas/" + personaId + "/analisis/indice.json", indice);
    }

    private void validarId(String id) {
        if (id == null || !ID_VALIDO.matcher(id).matches()) {
            throw new IllegalArgumentException("El ID debe contener solo letras minúsculas, números y guiones.");
        }
    }
}
