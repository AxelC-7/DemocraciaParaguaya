package com.demopy.controller;

import com.demopy.model.Candidato;
import com.demopy.service.CandidatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CandidatoController {

    private final CandidatoService candidatoService;

    public CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService;
    }

    @GetMapping("/candidatos/buscar")
    public ResponseEntity<Candidato> buscarCandidatoPorInstagram(
            @RequestParam(name = "nombre") String nombre) {

        if (nombre == null || nombre.isBlank() || nombre.length() > 150) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                candidatoService.procesarBusquedaConIA(nombre.trim())
        );
    }
}
