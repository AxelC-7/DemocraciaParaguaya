package com.demopy.controller;

import com.demopy.model.Persona;
import com.demopy.service.PersonaDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {
    private final PersonaDataService personaDataService;

    public PersonaController(PersonaDataService personaDataService) {
        this.personaDataService = personaDataService;
    }

    @GetMapping
    public ResponseEntity<List<Persona>> listar() throws IOException {
        return ResponseEntity.ok(personaDataService.listarPersonas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtener(@PathVariable String id) throws IOException {
        try {
            return ResponseEntity.ok(personaDataService.obtenerPersona(id));
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
