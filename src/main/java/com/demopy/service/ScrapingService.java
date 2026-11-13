package com.demopy.service;

import com.demopy.model.Candidato;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ScrapingService {
    @Value("\${gemini.api.key:}")
    private String apiKey;
    private final GeminiModelDiscoveryService modelDiscoveryService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScrapingService(GeminiModelDiscoveryService modelDiscoveryService) {
        this.modelDiscoveryService = modelDiscoveryService;
    }

    public Candidato investigarCandidatoConGemini(String nombreCandidato) {
        if (apiKey == null || apiKey.isBlank()) {
            return construirError(nombreCandidato,
                    "La investigación con IA no está disponible: falta configurar GEMINI_API_KEY.");
        }

        List<String> modelos = modelDiscoveryService.obtenerModelosGenerativosDisponibles();
        if (modelos.isEmpty()) {
            return construirError(nombreCandidato,
                    "No se encontraron modelos generativos disponibles para esta API key.");
        }

        String prompt = """
            Actúa como un analista de transparencia política especializado en Paraguay.
            Investiga al siguiente candidato o figura pública: "%s".

            Cubre trayectoria académica y profesional, cargos públicos, noticias de prensa
            y controversias documentadas. Incluye percepción ciudadana solo si tienes
            acceso a publicaciones concretas y verificables.

            No inventes noticias, citas, enlaces, procesos ni datos biográficos. Si no puedes
            verificar un dato, indícalo como no disponible. No afirmes haber consultado redes
            sociales si no tienes acceso a publicaciones concretas. No presentes porcentajes
            de sentimiento como estadísticas si no existe una muestra identificable y suficiente.

            Responde únicamente con un objeto JSON válido con esta estructura:
            {
              "id": "py-2026",
              "nombre": "Nombre completo del candidato",
              "partido": "Cargo actual, postulación o sector asociado; indicar si no se pudo verificar",
              "noticias": ["Hecho o noticia con fuente y fecha, o indicar que no fue verificado"],
              "redesSociales": ["Comentario verificable con plataforma y contexto, o indicar que no hay datos verificables"],
              "sentimientoGeneral": "Favorable / Mixto / Crítico / No determinado",
              "desgloseSentimiento": {
                "positivos": "No estimable sin muestra verificable",
                "negativos": "No estimable sin muestra verificable",
                "neutros": "No estimable sin muestra verificable"
              }
            }
            """.formatted(nombreCandidato);

        String ultimoError = "No se encontraron modelos disponibles.";
        for (String modelo : modelos) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";
            try {
                return ejecutarPeticionGemini(url, prompt);
            } catch (HttpClientErrorException e) {
                ultimoError = e.getStatusCode() + " en " + modelo;
                System.err.println("Falló " + ultimoError + ". Probando siguiente modelo...");
            } catch (Exception e) {
                ultimoError = e.getMessage() == null ? "Error inesperado" : e.getMessage();
                System.err.println("Error con " + modelo + ": " + ultimoError);
            }
        }

        return construirError(nombreCandidato,
                "Se probaron " + modelos.size() + " modelos y todos fallaron. Último error: " + ultimoError);
    }

    private Candidato ejecutarPeticionGemini(String url, String prompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty() || !parts.get(0).hasNonNull("text")) {
            throw new IllegalStateException("Gemini devolvió una respuesta sin contenido de texto.");
        }

        String textoRespuesta = parts.get(0).path("text").asText().trim();
        String fence = String.valueOf((char) 96).repeat(3);
        textoRespuesta = textoRespuesta.replace(fence + "json", "").replace(fence, "").trim();
        int inicioJson = textoRespuesta.indexOf("{");
        int finJson = textoRespuesta.lastIndexOf("}");
        if (inicioJson < 0 || finJson <= inicioJson) {
            throw new IllegalStateException("Gemini no devolvió un objeto JSON válido.");
        }
        return objectMapper.readValue(textoRespuesta.substring(inicioJson, finJson + 1), Candidato.class);
    }

    private Candidato construirError(String nombreCandidato, String detalle) {
        return new Candidato(
                "error-api", nombreCandidato, "Error en consulta IA",
                List.of(detalle), List.of("No se dispone de información verificable."),
                "No determinado",
                Map.of("positivos", "No estimable", "negativos", "No estimable", "neutros", "No estimable")
        );
    }
}
