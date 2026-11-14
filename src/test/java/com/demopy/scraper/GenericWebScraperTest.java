package com.demopy.scraper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericWebScraperTest {
    private final GenericWebScraper scraper = new GenericWebScraper();

    @Test
    void rechazaUrlsLocales() {
        assertThrows(IllegalArgumentException.class, () -> scraper.extraer("http://localhost:8080/"));
        assertThrows(IllegalArgumentException.class, () -> scraper.extraer("http://192.168.1.10/"));
    }

    @Test
    void rechazaProtocolosNoHttp() {
        assertThrows(IllegalArgumentException.class, () -> scraper.extraer("file:///etc/passwd"));
    }
}
