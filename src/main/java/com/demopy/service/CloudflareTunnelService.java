package com.demopy.service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "cloudflare.enabled", havingValue = "true")
public class CloudflareTunnelService {

    @Value("${cloudflare.executable:cloudflared}")
    private String executable;

    @Value("${cloudflare.origin:http://127.0.0.1:8080}")
    private String origin;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<String> currentUrl = new AtomicReference<>();
    private Process tunnelProcess;
    private Thread supervisorThread;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "https://[a-zA-Z0-9-]+\\.trycloudflare\\.com"
    );

    private final TelegramNotificationService telegramService;

    public CloudflareTunnelService(TelegramNotificationService telegramService) {
        this.telegramService = telegramService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTunnel() {
        if (!running.compareAndSet(false, true)) return;

        supervisorThread = new Thread(this::monitorTunnel, "cloudflare-tunnel-supervisor");
        supervisorThread.setDaemon(true);
        supervisorThread.start();
    }

    private void monitorTunnel() {
        int retry = 0;
        while (running.get()) {
            long startTime = System.currentTimeMillis();
            try {
                ProcessBuilder builder = new ProcessBuilder(
                        executable, "tunnel", "--no-autoupdate",
                        "--loglevel", "info", "--url", origin
                );
                builder.redirectErrorStream(true);
                tunnelProcess = builder.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(tunnelProcess.getInputStream()))) {
                    String line;
                    while (running.get() && (line = reader.readLine()) != null) {
                        detectUrl(line);
                    }
                }
                tunnelProcess.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Cloudflare] " + e.getMessage());
            }

            if (!running.get()) break;
            if (System.currentTimeMillis() - startTime > 30_000) retry = 0;
            retry++;
            long delay = Math.min(5_000L * (1L << Math.min(retry - 1, 4)), 60_000L);
            System.err.println("[Cloudflare] Túnel interrumpido. Reintento en " + delay / 1000 + " segundos.");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void detectUrl(String line) {
        Matcher matcher = URL_PATTERN.matcher(line);
        if (matcher.find()) {
            String newUrl = matcher.group();
            String previousUrl = currentUrl.getAndSet(newUrl);
            if (!newUrl.equals(previousUrl)) {
                System.out.println("[Cloudflare] URL pública: " + newUrl);
                telegramService.enviarUrlActualizada(newUrl, previousUrl);
            }
        }
    }

    public String getCurrentUrl() {
        return currentUrl.get();
    }

    @PreDestroy
    public void stopTunnel() {
        running.set(false);
        if (tunnelProcess != null && tunnelProcess.isAlive()) {
            tunnelProcess.destroy();
            try {
                if (!tunnelProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    tunnelProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                tunnelProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
        if (supervisorThread != null) supervisorThread.interrupt();
    }
}
