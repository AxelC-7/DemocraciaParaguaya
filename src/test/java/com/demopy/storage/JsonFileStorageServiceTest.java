package com.demopy.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileStorageServiceTest {

    @TempDir
    Path directorioTemporal;

    private JsonFileStorageService storage;

    @BeforeEach
    void configurar() {
        storage = new JsonFileStorageService(
                new ObjectMapper(),
                directorioTemporal.toString()
        );
    }

    @Test
    void guardaYLeeJsonEnDirectoriosAnidados() throws IOException {
        Map<String, Object> perfilOriginal = Map.of(
                "id", "jorge-schreiner",
                "nombre", "Jorge Schreiner"
        );

        storage.guardar("personas/jorge-schreiner/perfil.json", perfilOriginal);

        Map<?, ?> perfilLeido = storage.leer(
                "personas/jorge-schreiner/perfil.json",
                Map.class
        );

        assertEquals("jorge-schreiner", perfilLeido.get("id"));
        assertEquals("Jorge Schreiner", perfilLeido.get("nombre"));
        assertTrue(storage.existe("personas/jorge-schreiner/perfil.json"));
    }

    @Test
    void rechazaRutasQueIntentanSalirDelDirectorioDeDatos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> storage.guardar("../fuera.json", Map.of("dato", "no permitido"))
        );
    }

    @Test
    void rechazaArchivosQueNoSonJson() {
        assertThrows(
                IllegalArgumentException.class,
                () -> storage.guardar("personas/perfil.txt", Map.of("dato", "no permitido"))
        );
    }
}
