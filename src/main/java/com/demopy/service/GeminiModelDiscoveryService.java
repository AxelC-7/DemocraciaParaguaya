package com.demopy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GeminiModelDiscoveryService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Caché en memoria de la lista de modelos para no consultar /models en cada búsqueda
    private List<String> modelosCacheados = new ArrayList<>();

    public List<String> obtenerModelosGenerativosDisponibles() {
        if (!modelosCacheados.isEmpty()) {
            return modelosCacheados;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        List<String> modelosValidos = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode modelsNode = root.path("models");

            if (modelsNode.isArray()) {
                for (JsonNode model : modelsNode) {
                    String fullName = model.path("name").asText(); // Ej: "models/gemini-3.5-flash-lite"
                    String cleanName = fullName.replace("models/", "");

                    // Verificar que soporte generateContent
                    boolean soportaGenerateContent = false;
                    for (JsonNode method : model.path("supportedGenerationMethods")) {
                        if ("generateContent".equals(method.asText())) {
                            soportaGenerateContent = true;
                            break;
                        }
                    }

                    // Filtrar modelos de texto generativo (excluir embedding, tts, image, audio, aqa)
                    if (soportaGenerateContent && esModeloDeTextoValido(cleanName)) {
                        modelosValidos.add(cleanName);
                    }
                }
            }

            // Ordenar dando prioridad a modelos "lite" y "flash" (mayor cuota gratuita)
            modelosValidos.sort(Comparator.comparingInt(this::calcularPrioridad));
            this.modelosCacheados = modelosValidos;

            System.out.println("=== MODELOS GENERATIVOS DETECTADOS PARA TU API KEY (" + modelosValidos.size() + ") ===");
            modelosValidos.forEach(m -> System.out.println(" -> " + m));

        } catch (Exception e) {
            System.err.println("Error consultando la lista de modelos en Google AI: " + e.getMessage());
            // Respaldo mínimo si falla el listado
            return List.of("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash");
        }

        return modelosValidos;
    }

    private boolean esModeloDeTextoValido(String nombre) {
        String lower = nombre.toLowerCase();
        boolean esFamiliaGenerativa = lower.contains("gemini") || lower.contains("gemma");
        boolean esEspecializadoNoTexto = lower.contains("embedding")
                || lower.contains("tts")
                || lower.contains("image")
                || lower.contains("audio")
                || lower.contains("vision")
                || lower.contains("lyria")
                || lower.contains("aqa");

        return esFamiliaGenerativa && !esEspecializadoNoTexto;
    }

    private int calcularPrioridad(String nombre) {
        String lower = nombre.toLowerCase();
        if (lower.contains("flash-lite")) return 1; // Máxima prioridad (500+ RPD gratis)
        if (lower.contains("flash")) return 2;      // Segunda prioridad
        if (lower.contains("gemma")) return 3;      // Tercera prioridad (alta cuota RPD)
        return 4;                                   // Modelos Pro / Experimentales al final
    }
}