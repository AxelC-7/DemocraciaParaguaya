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

    @Value("${gemini.api.key}")
    private String apiKey;

    private final GeminiModelDiscoveryService modelDiscoveryService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ScrapingService(GeminiModelDiscoveryService modelDiscoveryService) {
        this.modelDiscoveryService = modelDiscoveryService;
    }

    public Candidato investigarCandidatoConGemini(String nombreCandidato) {
        List<String> modelos = modelDiscoveryService.obtenerModelosGenerativosDisponibles();

        String prompt = """
            Actúa como un analista de transparencia política especializado en Paraguay.
            Investiga en profundidad al siguiente candidato o figura pública: "%s".
            
            Cubre proactivamente TODOS los escenarios:
            1. Trayectoria académica, cargos públicos/privados, y cualquier información que lo describa personal y profesionalmente.
            2. Noticias resaltantes de prensa paraguaya (ABC, Última Hora, La Nación, etc.) y procesos judiciales o controversias.
            3. Comentarios y percepción ciudadana encontrados en publicaciones de redes sociales (Instagram, Facebook, X).
            
            Clasifica la percepción pública en comentarios Positivos, Negativos y Neutros.
            
            Responde ÚNICAMENTE con un objeto JSON válido (sin bloques de markdown ```json) con esta estructura exacta:
            {
              "id": "py-2026",
              "nombre": "Nombre completo del candidato",
              "partido": "Cargo actual, postulación o sector asociado",
              "noticias": ["Dato relevante 1", "Dato relevante 2", "Trayectoria académica/personal/profesional"],
              "redesSociales": ["5 Citas o resumenes de comentarios en Instagram/X/Facebook 1", "Mención 2"],
              "sentimientoGeneral": "Favorable / Mixto / Crítico",
              "desgloseSentimiento": {
                "positivos": "Resumen y porcentaje estimado de comentarios positivos",
                "negativos": "Resumen y porcentaje estimado de críticas o cuestionamientos",
                "neutros": "Resumen y porcentaje estimado de comentarios informativos o neutros"
              }
            }
            """.formatted(nombreCandidato);

        String ultimoError = "No se encontraron modelos disponibles.";

        for (String modelo : modelos) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent?key=" + apiKey;

            // Los modelos Gemma no soportan la herramienta google_search
            boolean soportaHerramientasWeb = modelo.toLowerCase().contains("gemini");

            // INTENTO A: Con Google Search Grounding (si el modelo lo soporta)
            /*if (soportaHerramientasWeb) {
                try {
                    System.out.println(">>> [INTENTO WEB] Probando modelo: " + modelo);
                    Candidato resultado = ejecutarPeticionGemini(url, prompt, true);
                    System.out.println("+++ [ÉXITO WEB] Respuesta obtenida con: " + modelo);
                    return resultado;
                } catch (HttpClientErrorException e) {
                    ultimoError = e.getStatusCode() + " en " + modelo + " (con Search)";
                    System.err.println("!!! Falló " + ultimoError + ". Probando modo estándar...");
                } catch (Exception e) {
                    ultimoError = e.getMessage();
                    System.err.println("!!! Error parseando/ejecutando " + modelo + ": " + ultimoError);
                }
            }*/

            // INTENTO B: Modo estándar sin herramienta web (consume cuota general RPD)
            try {
                System.out.println(">>> [INTENTO ESTÁNDAR] Probando modelo: " + modelo);
                Candidato resultado = ejecutarPeticionGemini(url, prompt, false);
                System.out.println("+++ [ÉXITO ESTÁNDAR] Respuesta obtenida con: " + modelo);
                return resultado;
            } catch (HttpClientErrorException e) {
                ultimoError = e.getStatusCode() + " en " + modelo + " (Estándar)";
                System.err.println("!!! Falló " + ultimoError + ". Saltando al siguiente modelo...");
            } catch (Exception e) {
                ultimoError = e.getMessage();
                System.err.println("!!! Error parseando respuesta de " + modelo + ": " + ultimoError);
            }
        }

        return construirError(nombreCandidato, "Se probaron " + modelos.size() + " modelos y todos fallaron. Último error: " + ultimoError);
    }

    private Candidato ejecutarPeticionGemini(String url, String prompt, boolean usarGoogleSearch) throws Exception {
        Map<String, Object> requestBody = usarGoogleSearch
                ? Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "tools", List.of(Map.of("google_search", Map.of()))
        )
                : Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        String textoRespuesta = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Limpiar posibles bloques de código markdown si el modelo los devuelve
        textoRespuesta = textoRespuesta.replace("```json", "").replace("```", "").trim();

        // Extraer el bloque JSON en caso de que algún modelo (ej. Gemma) agregue texto antes o después de las llaves
        int inicioJson = textoRespuesta.indexOf("{");
        int finJson = textoRespuesta.lastIndexOf("}");
        if (inicioJson != -1 && finJson != -1 && finJson > inicioJson) {
            textoRespuesta = textoRespuesta.substring(inicioJson, finJson + 1);
        }

        return objectMapper.readValue(textoRespuesta, Candidato.class);
    }

    private Candidato construirError(String nombreCandidato, String detalle) {
        return new Candidato(
                "error-api",
                nombreCandidato,
                "Error en consulta IA",
                List.of(detalle),
                List.of("Revisa los logs en la consola de IntelliJ para ver qué modelos fueron detectados"),
                "Sin datos",
                Map.of("positivos", "0%", "negativos", "0%", "neutros", "0%")
        );
    }
}