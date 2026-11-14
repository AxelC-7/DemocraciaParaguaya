package com.demopy.service;

import com.demopy.model.Persona;
import com.demopy.model.Postulacion;
import com.demopy.model.Propuesta;
import com.demopy.model.Testimonio;
import com.demopy.model.Trayectoria;
import com.demopy.storage.JsonFileStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PersonaDataService {
    private static final Pattern ID_VALIDO = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private final JsonFileStorageService storage;

    public PersonaDataService(JsonFileStorageService storage) {
        this.storage = storage;
    }

    public void guardarPersona(Persona persona) throws IOException {
        if (persona == null) throw new IllegalArgumentException("La persona es obligatoria.");
        validarId(persona.getId());
        storage.guardar(ruta(persona.getId(), "perfil.json"), persona);
        actualizarIndice(persona);
    }

    public List<Persona> listarPersonas() throws IOException {
        String indice = "personas/indice.json";
        if (!storage.existe(indice)) return new ArrayList<>();
        return storage.leer(indice, new TypeReference<List<Persona>>() {});
    }

    private void actualizarIndice(Persona persona) throws IOException {
        List<Persona> personas = new ArrayList<>(listarPersonas());
        personas.removeIf(actual -> persona.getId().equals(actual.getId()));
        personas.add(persona);
        storage.guardar("personas/indice.json", personas);
    }

    public Persona obtenerPersona(String personaId) throws IOException {
        validarId(personaId);
        return storage.leer(ruta(personaId, "perfil.json"), Persona.class);
    }

    public void guardarTrayectoria(String personaId, Trayectoria trayectoria) throws IOException {
        validarId(personaId);
        if (trayectoria == null) throw new IllegalArgumentException("La trayectoria es obligatoria.");
        if (trayectoria.getPersonaId() != null && !personaId.equals(trayectoria.getPersonaId())) {
            throw new IllegalArgumentException("El personaId de la trayectoria no coincide.");
        }
        trayectoria.setPersonaId(personaId);
        storage.guardar(ruta(personaId, "trayectoria.json"), trayectoria);
    }

    public Trayectoria obtenerTrayectoria(String personaId) throws IOException {
        validarId(personaId);
        return storage.leer(ruta(personaId, "trayectoria.json"), Trayectoria.class);
    }

    public void guardarPostulaciones(String personaId, List<Postulacion> datos) throws IOException {
        guardarColeccion(personaId, "postulaciones.json", datos, Postulacion::getPersonaId);
    }

    public List<Postulacion> obtenerPostulaciones(String personaId) throws IOException {
        return leerColeccion(personaId, "postulaciones.json", new TypeReference<>() {});
    }

    public void guardarPropuestas(String personaId, List<Propuesta> datos) throws IOException {
        guardarColeccion(personaId, "propuestas.json", datos, Propuesta::getPersonaId);
    }

    public List<Propuesta> obtenerPropuestas(String personaId) throws IOException {
        return leerColeccion(personaId, "propuestas.json", new TypeReference<>() {});
    }

    public void guardarTestimonios(String personaId, List<Testimonio> datos) throws IOException {
        guardarColeccion(personaId, "testimonios.json", datos, Testimonio::getPersonaId);
    }

    public List<Testimonio> obtenerTestimonios(String personaId) throws IOException {
        return leerColeccion(personaId, "testimonios.json", new TypeReference<>() {});
    }

    private <T> void guardarColeccion(String personaId, String archivo, List<T> datos,
                                     java.util.function.Function<T, String> obtenerPersonaId) throws IOException {
        validarId(personaId);
        if (datos == null) throw new IllegalArgumentException("La colección no puede ser null.");
        for (T dato : datos) {
            if (dato == null) throw new IllegalArgumentException("La colección contiene un elemento null.");
            String idRelacionado = obtenerPersonaId.apply(dato);
            if (idRelacionado != null && !personaId.equals(idRelacionado)) {
                throw new IllegalArgumentException("Un registro pertenece a otra persona.");
            }
        }
        storage.guardar(ruta(personaId, archivo), datos);
    }

    private <T> List<T> leerColeccion(String personaId, String archivo, TypeReference<List<T>> tipo)
            throws IOException {
        validarId(personaId);
        String ruta = ruta(personaId, archivo);
        if (!storage.existe(ruta)) return new ArrayList<>();
        return storage.leer(ruta, tipo);
    }

    private String ruta(String personaId, String archivo) {
        return "personas/" + personaId + "/" + archivo;
    }

    private void validarId(String personaId) {
        if (personaId == null || !ID_VALIDO.matcher(personaId).matches()) {
            throw new IllegalArgumentException("El ID debe contener solo letras minúsculas, números y guiones.");
        }
    }
}
