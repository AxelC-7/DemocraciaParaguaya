package com.demopy.scraper;

import com.demopy.model.Fuente;
import com.demopy.model.Noticia;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Locale;

@Component
public class GenericWebScraper {
    @Value("${demopy.scraper.user-agent:DemocraciaParaguayaBot/1.0 (contact: admin@example.invalid)}")
    private String userAgent;

    @Value("${demopy.scraper.timeout-ms:15000}")
    private int timeoutMs;

    public Noticia extraer(String url) throws IOException {
        validarUrl(url);
        Document doc = Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer("https://www.google.com/")
                .timeout(timeoutMs)
                .maxBodySize(2 * 1024 * 1024)
                .followRedirects(true)
                .get();

        String canonical = obtenerCanonical(doc, url);
        Noticia noticia = new Noticia();
        noticia.setUrl(canonical);
        noticia.setTitulo(primerValor(
                meta(doc, "og:title"), meta(doc, "twitter:title"),
                texto(doc, "h1"), doc.title()));
        noticia.setResumen(primerValor(
                meta(doc, "description"), meta(doc, "og:description"),
                meta(doc, "twitter:description")));
        noticia.setContenido(extraerContenido(doc));
        noticia.setMedio(host(canonical));
        noticia.setAutor(primerValor(
                meta(doc, "author"), meta(doc, "article:author"),
                texto(doc, "[rel=author]")));
        noticia.setFechaPublicacion(primerValor(
                meta(doc, "article:published_time"),
                meta(doc, "datePublished"),
                contenido(doc, "time[datetime]")));
        noticia.setFechaRecoleccion(Instant.now().toString());
        noticia.setFuentes(java.util.List.of(new Fuente(noticia.getTitulo(), canonical)));
        return noticia;
    }

    private String extraerContenido(Document doc) {
        String[] selectores = {
                "article", "[itemprop=articleBody]", "main",
                ".article-body", ".article-content", ".post-content",
                ".entry-content", ".content"
        };
        for (String selector : selectores) {
            Elements elementos = doc.select(selector);
            for (Element elemento : elementos) {
                String texto = elemento.text().trim();
                if (texto.length() >= 250) return limitar(texto, 30000);
            }
        }
        Element body = doc.body();
        if (body == null) return "";
        body.select("nav, header, footer, aside, script, style, noscript, form").remove();
        return limitar(body.text().trim(), 30000);
    }

    private String meta(Document doc, String clave) {
        Element e = doc.select("meta[property=" + clave + "], meta[name=" + clave + "]").first();
        return e == null ? null : e.attr("content").trim();
    }

    private String texto(Document doc, String selector) {
        Element e = doc.select(selector).first();
        return e == null ? null : e.text().trim();
    }

    private String contenido(Document doc, String selector) {
        Element e = doc.select(selector).first();
        if (e == null) return null;
        String value = e.attr("datetime");
        return value.isBlank() ? e.text().trim() : value;
    }

    private String obtenerCanonical(Document doc, String original) {
        Element e = doc.select("link[rel=canonical]").first();
        if (e == null || e.attr("href").isBlank()) return original;
        try {
            return URI.create(original).resolve(e.attr("href")).toString();
        } catch (IllegalArgumentException ex) {
            return original;
        }
    }

    private String primerValor(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) return valor.trim();
        }
        return "";
    }

    private String host(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception ex) {
            return "";
        }
    }

    private String limitar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }

    private void validarUrl(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("La URL es obligatoria.");
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("La URL no es válida.");
        }
        String scheme = uri.getScheme();
        if (uri.getHost() == null || scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Solo se permiten URLs HTTP o HTTPS.");
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.equals("localhost") || host.equals("127.0.0.1") || host.equals("0.0.0.0")
                || host.startsWith("10.") || host.startsWith("192.168.")
                || host.equals("::1") || host.endsWith(".local")) {
            throw new IllegalArgumentException("No se permiten direcciones locales.");
        }
    }
}
