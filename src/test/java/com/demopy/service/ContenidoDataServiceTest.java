package com.demopy.service;

import com.demopy.model.Analisis;
import com.demopy.model.Noticia;
import com.demopy.storage.JsonFileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContenidoDataServiceTest {
    @TempDir Path directorioTemporal;
    private ContenidoDataService service;

    @BeforeEach
    void configurar() {
        service = new ContenidoDataService(
                new JsonFileStorageService(new ObjectMapper(), directorioTemporal.toString()));
    }

    @Test
    void guardaYRecuperaNoticia() throws IOException {
        Noticia noticia = new Noticia();
        noticia.setId("noticia-001");
        noticia.setTitulo("Noticia de prueba");
        service.guardarNoticia(noticia);
        assertEquals("Noticia de prueba", service.obtenerNoticia("noticia-001").getTitulo());
    }

    @Test
    void guardaAnalisisYActualizaIndiceSinDuplicar() throws IOException {
        Analisis a = new Analisis();
        a.setId("analisis-001");
        a.setResumen("Primer análisis");
        service.guardarAnalisisEIndice("persona-prueba", a);
        a.setResumen("Análisis actualizado");
        service.guardarAnalisisEIndice("persona-prueba", a);

        List<Analisis> lista = service.listarAnalisis("persona-prueba");
        assertEquals(1, lista.size());
        assertEquals("Análisis actualizado", lista.get(0).getResumen());
        assertEquals("Análisis actualizado",
                service.obtenerAnalisis("persona-prueba", "analisis-001").getResumen());
    }

    @Test
    void rechazaIdInvalido() {
        Noticia noticia = new Noticia();
        noticia.setId("../archivo");
        assertThrows(IllegalArgumentException.class, () -> service.guardarNoticia(noticia));
    }
}
