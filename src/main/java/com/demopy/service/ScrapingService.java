package com.demopy.service;

import com.demopy.model.Candidato;
import com.demopy.model.CitaFuente;
import com.demopy.model.Fuente;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
            Investiga a "%s", figura pública o candidato de Paraguay, usando búsqueda web.
            Prioriza fuentes primarias y medios periodísticos reconocidos. Busca información
            reciente y también antecedentes relevantes. Distingue hechos confirmados,
            alegaciones, opiniones y datos que no pudieron verificarse.

            No inventes hechos, fechas, citas, procesos judiciales ni fuentes. No atribuyas
            afiliaciones o cargos actuales sin respaldo. Si hay homónimos, explica la duda
            y no mezcles sus antecedentes. No afirmes haber leído comentarios de redes
            sociales ni hagas análisis de sentimiento: esta aplicación no recopila comentarios.
            En caso de no hallar evidencia suficiente, dilo expresamente.

            Devuelve únicamente un objeto JSON válido con esta estructura:
            {
              "id": "py-2026",
              "nombre": "Nombre de la persona investigada",
              "partido": "Partido, cargo o afiliación solo si se pudo verificar; de lo contrario indicar No verificado",
              "noticias": [
                "Resumen breve de un hecho verificable, indicando fecha y contexto"
              ],
              "redesSociales": [
                "No se recopilan comentarios de redes sociales en esta versión"
              ],
              "sentimientoGeneral": "No evaluado",
              "desgloseSentimiento": {
                "positivos": "No evaluado",
                "negativos": "No evaluado",
                "neutros": "No evaluado"
              }
            }
            No incluyas URLs inventadas en el JSON. Las fuentes se obtendrán de los metadatos
            de Google Search que devuelve la API.
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
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "tools", List.of(Map.of("google_search", Map.of()))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode candidate = root.path("candidates").path(0);
        JsonNode parts = candidate.path("content").path("parts");
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

        Candidato candidato = objectMapper.readValue(
                textoRespuesta.substring(inicioJson, finJson + 1), Candidato.class);
        aplicarMetadatosDeFuentes(candidate, candidateNode(candidate, root));
        return candidato;
    }

    private JsonNode candidateNode(Candidato ignored, JsonNode root) {
        return root.path("candidates").path(0);
    }

    void aplicarMetadatosDeFuentes(Candidato candidato, JsonNode candidate) {
        JsonNode metadata = candidate.path("groundingMetadata");
        JsonNode chunks = metadata.path("groundingChunks");
        List<Fuente> fuentes = new ArrayList<>();
        Map<String, Fuente> fuentesUnicas = new LinkedHashMap<>();

        if (chunks.isArray()) {
            for (JsonNode chunk : chunks) {
                JsonNode web = chunk.path("web");
                String url = web.path("uri").asText("");
                String titulo = web.path("title").asText("");
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    fuentesUnicas.putIfAbsent(url, new Fuente(
                            titulo.isBlank() ? url : titulo, url));
                }
            }
        }
        fuentes.addAll(fuentesUnicas.values());
        candidato.setFuentes(fuentes);

        List<CitaFuente> citas = new ArrayList<>();
        JsonNode supports = metadata.path("groundingSupports");
        if (supports.isArray()) {
            for (JsonNode support : supports) {
                String texto = support.path("segment").path("text").asText("");
                JsonNode indices = support.path("groundingChunkIndices");
                if (texto.isBlank() || !indices.isArray()) continue;

                List<Fuente> fuentesCita = new ArrayList<>();
                for (JsonNode indexNode : indices) {
                    int index = indexNode.asInt(-1);
                    if (index >= 0 && index < chunks.size()) {
                        JsonNode web = chunks.get(index).path("web");
                        String url = web.path("uri").asText("");
                        String titulo = web.path("title").asText("");
                        if (url.startsWith("https://") || url.startsWith("http://")) {
                            fuentesCita.add(new Fuente(titulo.isBlank() ? url : titulo, url));
                        }
                    }
                }
                if (!fuentesCita.isEmpty()) {
                    citas.add(new CitaFuente(texto, fuentesCita));
                }
            }
        }
        candidato.setCitas(citas);

        String searchSuggestions = metadata.path("searchEntryPoint")
                .path("renderedContent").asText(null);
        candidato.setSearchEntryPointHtml(searchSuggestions);
    }

    private Candidato construirError(String nombreCandidato, String detalle) {
        return new Candidato(
                "error-api", nombreCandidato, "Error en consulta IA",
                List.of(detalle), List.of("No se dispone de información verificable."),
                "No evaluado",
                Map.of("positivos", "No evaluado", "negativos", "No evaluado", "neutros", "No evaluado")
        );
    }
}
