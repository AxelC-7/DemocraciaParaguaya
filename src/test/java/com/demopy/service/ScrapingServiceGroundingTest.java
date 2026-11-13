package com.demopy.service;

import com.demopy.model.Candidato;
import com.demopy.model.Fuente;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ScrapingServiceGroundingTest {

    private final ScrapingService service =
            new ScrapingService(mock(GeminiModelDiscoveryService.class));
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void extraeFuentesUnicasDesdeGroundingMetadata() throws Exception {
        JsonNode candidate = mapper.readTree("""
                {
                  "groundingMetadata": {
                    "groundingChunks": [
                      {"web": {"title": "Fuente A", "uri": "https://example.com/a"}},
                      {"web": {"title": "Fuente A duplicada", "uri": "https://example.com/a"}},
                      {"web": {"title": "Fuente B", "uri": "https://example.com/b"}},
                      {"web": {"title": "URL insegura", "uri": "javascript:alert(1)"}}
                    ],
                    "searchEntryPoint": {
                      "renderedContent": "<div>Search suggestions</div>"
                    }
                  }
                }
                """);
        Candidato candidato = new Candidato(
                "1", "Candidato", "Partido", List.of(), List.of(),
                "No evaluado", Map.of());

        service.aplicarMetadatosDeFuentes(candidato, candidate);

        assertEquals(2, candidato.getFuentes().size());
        assertEquals("Fuente A", candidato.getFuentes().get(0).getTitulo());
        assertEquals("https://example.com/a", candidato.getFuentes().get(0).getUrl());
        assertEquals("<div>Search suggestions</div>", candidato.getSearchEntryPointHtml());
    }

    @Test
    void respondeConListaVaciaCuandoNoHayGroundingMetadata() throws Exception {
        Candidato candidato = new Candidato(
                "1", "Candidato", "Partido", List.of(), List.of(),
                "No evaluado", Map.of());

        service.aplicarMetadatosDeFuentes(candidato, mapper.readTree("{}"));

        assertNotNull(candidato.getFuentes());
        assertTrue(candidato.getFuentes().isEmpty());
        assertNull(candidato.getSearchEntryPointHtml());
    }
}
