package com.demopy.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class TelegramNotificationService {
    private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final RestClient restClient;

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.chat-id:}")
    private String chatId;

    public TelegramNotificationService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public boolean enviarUrlActualizada(String nuevaUrl, String urlAnterior) {
        if (nuevaUrl == null || nuevaUrl.isBlank()) {
            log.warn("No se puede enviar una URL vacía.");
            return false;
        }
        if (nuevaUrl.equals(urlAnterior)) {
            log.info("La URL no ha cambiado. No se envía notificación.");
            return false;
        }
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.debug("Telegram no está configurado; se omite la notificación.");
            return false;
        }

        String mensaje = """
                🚀 DEMOCRACIA PARAGUAYA

                El servidor se encuentra disponible.

                🔗 Nueva URL pública:
                %s

                %s
                """.formatted(nuevaUrl, urlAnterior == null
                ? "Primera conexión del túnel."
                : "La URL anterior ha sido reemplazada.");

        String telegramApi = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        try {
            Map<?, ?> respuesta = restClient.post()
                    .uri(telegramApi)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", mensaje))
                    .retrieve()
                    .body(Map.class);

            if (respuesta != null && Boolean.TRUE.equals(respuesta.get("ok"))) {
                log.info("Notificación enviada correctamente a Telegram.");
                return true;
            }
            log.error("Telegram rechazó el envío: {}", respuesta);
            return false;
        } catch (RestClientException e) {
            log.error("Error enviando notificación a Telegram: {}", e.getMessage());
            return false;
        }
    }
}
