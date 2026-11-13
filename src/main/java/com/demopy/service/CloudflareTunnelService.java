package com.demopy.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CloudflareTunnelService {

    @Value("${cloudflare.executable:C:/Users/AXEL/Downloads/cloudflared-windows-amd64.exe}")
    private String executable;

    @Value("${cloudflare.origin:http://127.0.0.1:8080}")
    private String origin;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AtomicReference<String> currentUrl =
            new AtomicReference<>();

    private Process tunnelProcess;
    private Thread supervisorThread;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https://[a-zA-Z0-9-]+\\.trycloudflare\\.com"
    );

    private final TelegramNotificationService telegramService;

    public CloudflareTunnelService(
            TelegramNotificationService telegramService) {
        this.telegramService = telegramService;
    }

    /**
     * Inicia el supervisor cuando Spring Boot está listo.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startTunnel() {

        if (!running.compareAndSet(false, true)) {
            return;
        }

        System.out.println("====================================");
        System.out.println(" INICIANDO CLOUDFLARE TUNNEL");
        System.out.println("====================================");

        supervisorThread = new Thread(
                this::monitorTunnel,
                "cloudflare-tunnel-supervisor"
        );

        supervisorThread.start();
    }

    /**
     * Supervisa el proceso y lo reinicia cuando se interrumpe.
     */
    private void monitorTunnel() {

        int retry = 0;

        while (running.get()) {

            long startTime = System.currentTimeMillis();

            try {

                System.out.println(
                        "[Cloudflare] Iniciando túnel..."
                );

                ProcessBuilder builder = new ProcessBuilder(
                        executable,
                        "tunnel",
                        "--no-autoupdate",
                        "--loglevel",
                        "info",
                        "--url",
                        origin
                );

                builder.redirectErrorStream(true);

                tunnelProcess = builder.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                tunnelProcess.getInputStream()
                        ))) {

                    String line;

                    while (running.get() &&
                            (line = reader.readLine()) != null) {

                        System.out.println(
                                "[Cloudflare] " + line
                        );

                        detectUrl(line);
                    }
                }

                tunnelProcess.waitFor();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                break;

            } catch (Exception e) {

                System.err.println(
                        "[Cloudflare] Error: " + e.getMessage()
                );
            }

            if (!running.get()) {
                break;
            }

            long uptime = System.currentTimeMillis() - startTime;

            // Si funcionó durante más de 30 segundos,
            // reiniciamos el contador de reintentos.
            if (uptime > 30_000) {
                retry = 0;
            }

            retry++;

            // Espera progresiva: 5, 10, 20, 40 y máximo 60 segundos.
            long delay = Math.min(
                    5_000L * (1L << Math.min(retry - 1, 4)),
                    60_000L
            );

            System.err.println(
                    "[Cloudflare] El túnel se interrumpió."
            );

            System.out.println(
                    "[Cloudflare] Nuevo intento en "
                            + (delay / 1000) + " segundos..."
            );

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("[Cloudflare] Supervisor detenido.");
    }

    /**
     * Detecta la URL pública en los registros de cloudflared.
     */
    private void detectUrl(String line) {

        Matcher matcher = URL_PATTERN.matcher(line);

        if (matcher.find()) {

            String newUrl = matcher.group();

            String previousUrl = currentUrl.getAndSet(newUrl);

            if (!newUrl.equals(previousUrl)) {

                System.out.println("");
                System.out.println(
                        "===================================="
                );
                System.out.println(
                        " NUEVA URL PUBLICA DE DEMOCRACIA"
                );
                System.out.println(
                        newUrl
                );
                System.out.println(
                        "===================================="
                );
                System.out.println("");

                onTunnelUrlChanged(newUrl, previousUrl);
            }
        }
    }

    /**
     * Punto de extensión para futuras integraciones.
     * Aquí implementaremos el envío del correo electrónico.
     */
    private void onTunnelUrlChanged(
            String newUrl,
            String previousUrl) {

        System.out.println(
                "[Cloudflare] URL actualizada correctamente."
        );

        if (previousUrl != null) {
            System.out.println(
                    "[Cloudflare] URL anterior: " + previousUrl
            );
        }

        telegramService.enviarUrlActualizada(
                newUrl,
                previousUrl
        );
    }

    /**
     * Permite consultar la URL pública desde otros servicios.
     */
    public String getCurrentUrl() {
        return currentUrl.get();
    }

    /**
     * Detiene correctamente el túnel al cerrar Spring Boot.
     */
    @PreDestroy
    public void stopTunnel() {

        running.set(false);

        if (tunnelProcess != null && tunnelProcess.isAlive()) {

            tunnelProcess.destroy();

            try {
                if (!tunnelProcess.waitFor(
                        5, java.util.concurrent.TimeUnit.SECONDS)) {

                    tunnelProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                tunnelProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }

        if (supervisorThread != null) {
            supervisorThread.interrupt();
        }

        System.out.println(
                "[Cloudflare] Túnel detenido por cierre de Spring Boot."
        );
    }
}
