package com.demopy.service;

import com.demopy.model.ResultadoBusqueda;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DuckDuckGoSearchService {
    @Value("${demopy.scraper.user-agent:DemocraciaParaguayaBot/1.0}")
    private String userAgent;

    @Value("${demopy.scraper.timeout-ms:15000}")
    private int timeoutMs;

    public List<ResultadoBusqueda> buscar(String consulta, int limite) throws IOException {
        if (consulta == null || consulta.isBlank()) throw new IllegalArgumentException("La consulta es obligatoria.");
        if (limite < 1 || limite > 30) throw new IllegalArgumentException("El límite debe estar entre 1 y 30.");

        String endpoint = "https://html.duckduckgo.com/html/?q="
                + java.net.URLEncoder.encode(consulta, StandardCharsets.UTF_8);
        Document doc = Jsoup.connect(endpoint)
                .userAgent(userAgent)
                .timeout(timeoutMs)
                .maxBodySize(1024 * 1024)
                .get();

        Map<String, ResultadoBusqueda> unicos = new LinkedHashMap<>();
        Elements resultados = doc.select(".result");
        for (Element resultado : resultados) {
            Element enlace = resultado.select(".result__title a, .result__a").first();
            if (enlace == null) continue;
            String url = resolverUrl(enlace.attr("href"));
            if (!(url.startsWith("http://") || url.startsWith("https://"))) continue;
            String titulo = enlace.text().trim();
            Element fragmento = resultado.select(".result__snippet").first();
            String resumen = fragmento == null ? "" : fragmento.text().trim();
            if (!titulo.isBlank()) {
                unicos.putIfAbsent(url, new ResultadoBusqueda(titulo, url, resumen, "DuckDuckGo HTML"));
            }
            if (unicos.size() >= limite) break;
        }
        return new ArrayList<>(unicos.values());
    }

    private String resolverUrl(String href) {
        if (href == null || href.isBlank()) return "";
        if (href.startsWith("//")) return "https:" + href;
        if (!href.startsWith("/l/?")) return href;
        try {
            String query = href.substring(href.indexOf('?') + 1);
            for (String parametro : query.split("&")) {
                String[] partes = parametro.split("=", 2);
                if (partes.length == 2 && partes[0].equals("uddg")) {
                    return URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }
}
