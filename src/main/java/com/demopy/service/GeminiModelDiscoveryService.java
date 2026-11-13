package com.demopy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GeminiModelDiscoveryService {

    @Value("\${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile List<String> modelosCacheados = List.of();

    public List<String> obtenerModelosGenerativosDisponibles() {
        if (apiKey == null || apiKey.isBlank()) return List.of();
        if (!modelosCacheados.isEmpty()) return modelosCacheados;

        String url = "https://generativelanguage.googleapis.com/v1beta/models";
        List<String> modelosValidos = new ArrayList<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-goog-api-key", apiKey);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET,
                    new org.springframework.http.HttpEntity<>(headers), String.class);

            JsonNode modelsNode = objectMapper.readTree(response.getBody()).path("models");
            if (modelsNode.isArray()) {
                for (JsonNode model : modelsNode) {
                    String cleanName = model.path("name").asText().replace("models/", "");
                    boolean supportsGenerateContent = false;
                    for (JsonNode method : model.path("supportedGenerationMethods")) {
                        if ("generateContent".equals(method.asText())) {
                            supportsGenerateContent = true;
                            break;
                        }
                    }
                    if (supportsGenerateContent && esModeloDeTextoValido(cleanName)) {
                        modelosValidos.add(cleanName);
                    }
                }
            }

            modelosValidos.sort(Comparator.comparingInt(this::calcularPrioridad));
            modelosCacheados = List.copyOf(modelosValidos);
            System.out.println("Modelos generativos detectados: " + modelosValidos.size());
        } catch (Exception e) {
            System.err.println("Error consultando modelos de Gemini: " + e.getMessage());
            return List.of();
        }
        return modelosCacheados;
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
        if (lower.contains("flash-lite")) return 1;
        if (lower.contains("flash")) return 2;
        if (lower.contains("gemma")) return 3;
        return 4;
    }
}
