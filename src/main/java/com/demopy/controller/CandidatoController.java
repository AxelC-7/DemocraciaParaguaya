package com.demopy.controller;

import com.demopy.model.Candidato;
import com.demopy.service.CandidatoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen local
public class CandidatoController {

    private final CandidatoService candidatoService;

    public CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService;
    }

    @GetMapping("/candidatos/buscar")
    public Candidato buscarCandidatoPorInstagram(@RequestParam String nombre) {
        // Recibe el parámetro desde la URL, ej: /api/candidatos/buscar?nombre=presidente_bcp_2026
        return candidatoService.procesarBusquedaConIA(nombre);
    }
}