package com.demopy.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleNewsScraper {

    public static void buscarMenciones(String nombreCandidato) {
        try {
            String url = "https://html.duckduckgo.com/html/?q=" + nombreCandidato + "+noticias";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            Elements links = doc.select(".result__title a");
            for (Element link : links) {
                System.out.println("Titular: " + link.text());
                System.out.println("Enlace: " + link.attr("href"));
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando scraping: " + e.getMessage());
        }
    }

    /*public static void main(String[] args) {
        buscarMenciones("Candidato Presidente BCP");
    }*/
}