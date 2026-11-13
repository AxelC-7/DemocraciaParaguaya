package com.demopy.service;

import com.demopy.model.Candidato;
import org.springframework.stereotype.Service;

@Service
public class CandidatoService {

    private final ScrapingService scrapingService;

    public CandidatoService(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    public Candidato procesarBusquedaConIA(String nombreCandidato) {
        return scrapingService.investigarCandidatoConGemini(nombreCandidato);
    }
}