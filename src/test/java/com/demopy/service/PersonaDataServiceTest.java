package com.demopy.service;

import com.demopy.model.Persona;
import com.demopy.model.Postulacion;
import com.demopy.model.Propuesta;
import com.demopy.model.Trayectoria;
import com.demopy.storage.JsonFileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonaDataServiceTest {
    @TempDir
    Path directorioTemporal;

    private PersonaDataService service;

    @BeforeEach
    void configurar() {
        service = new PersonaDataService(
                new JsonFileStorageService(new ObjectMapper(), directorioTemporal.toString())
        );
    }

    @Test
    void guardaYRecuperaPerfilDePersona() throws IOException {
        Persona persona = new Persona();
        persona.setId("jorge-schreiner");
        persona.setNombreCompleto("Jorge Schreiner");

        service.guardarPersona(persona);

        assertEquals("Jorge Schreiner", service.obtenerPersona("jorge-schreiner").getNombreCompleto());
        assertTrue(directorioTemporal.resolve("personas/jorge-schreiner/perfil.json").toFile().isFile());
        assertEquals(1, service.listarPersonas().size());
    }

    @Test
    void actualizarPerfilNoDuplicaElIndice() throws IOException {
        Persona persona = new Persona();
        persona.setId("jorge-schreiner");
        persona.setNombreCompleto("Nombre inicial");
        service.guardarPersona(persona);

        persona.setNombreCompleto("Nombre actualizado");
        service.guardarPersona(persona);

        assertEquals(1, service.listarPersonas().size());
        assertEquals("Nombre actualizado", service.listarPersonas().get(0).getNombreCompleto());
    }

    @Test
    void guardaTrayectoriaYAsignaPersonaId() throws IOException {
        Trayectoria trayectoria = new Trayectoria();

        service.guardarTrayectoria("jorge-schreiner", trayectoria);

        assertEquals("jorge-schreiner", service.obtenerTrayectoria("jorge-schreiner").getPersonaId());
    }

    @Test
    void coleccionesInexistentesSeDevuelvenVacias() throws IOException {
        assertTrue(service.obtenerPostulaciones("jorge-schreiner").isEmpty());
        assertTrue(service.obtenerPropuestas("jorge-schreiner").isEmpty());
        assertTrue(service.obtenerTestimonios("jorge-schreiner").isEmpty());
    }

    @Test
    void guardaPostulacionesYPropuestas() throws IOException {
        Postulacion postulacion = new Postulacion();
        postulacion.setId("postulacion-bcp");
        postulacion.setPersonaId("jorge-schreiner");
        postulacion.setCargo("Presidente del Banco Central del Paraguay");

        Propuesta propuesta = new Propuesta();
        propuesta.setId("propuesta-001");
        propuesta.setPersonaId("jorge-schreiner");
        propuesta.setTitulo("Ejemplo de propuesta");

        service.guardarPostulaciones("jorge-schreiner", List.of(postulacion));
        service.guardarPropuestas("jorge-schreiner", List.of(propuesta));

        assertEquals(1, service.obtenerPostulaciones("jorge-schreiner").size());
        assertEquals("Ejemplo de propuesta", service.obtenerPropuestas("jorge-schreiner").get(0).getTitulo());
    }

    @Test
    void rechazaIdentificadoresConRutas() {
        Persona persona = new Persona();
        persona.setId("../otro-directorio");

        assertThrows(IllegalArgumentException.class, () -> service.guardarPersona(persona));
    }

    @Test
    void rechazaRegistrosDeOtraPersona() {
        Postulacion postulacion = new Postulacion();
        postulacion.setPersonaId("otra-persona");

        assertThrows(IllegalArgumentException.class,
                () -> service.guardarPostulaciones("jorge-schreiner", List.of(postulacion)));
    }
}
