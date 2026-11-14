package com.demopy.controller;

import com.demopy.model.Noticia;
import com.demopy.service.ContenidoDataService;
import com.demopy.service.NewsCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NewsCollectionController {
    private final NewsCollectionService collectionService;
    private final ContenidoDataService contenidoService;

    public NewsCollectionController(NewsCollectionService collectionService,
                                    ContenidoDataService contenidoService) {
        this.collectionService = collectionService;
        this.contenidoService = contenidoService;
    }

    @PostMapping("/recoleccion/noticias")
    public ResponseEntity<?> recolectar(@RequestBody SolicitudRecoleccion solicitud) {
        if (solicitud == null || solicitud.consulta() == null || solicitud.consulta().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La consulta es obligatoria."));
        }
        String consulta = solicitud.consulta().trim();
        if (consulta.length() > 200) {
            return ResponseEntity.badRequest().body(Map.of("error", "La consulta no puede superar 200 caracteres."));
        }
        int limite = solicitud.limite() == null ? 5 : solicitud.limite();
        if (limite < 1 || limite > 10) {
            return ResponseEntity.badRequest().body(Map.of("error", "El límite debe estar entre 1 y 10."));
        }
        try {
            List<Noticia> noticias = collectionService.buscarYRecolectar(consulta, limite);
            return ResponseEntity.ok(Map.of(
                    "consulta", consulta,
                    "cantidad", noticias.size(),
                    "noticias", noticias
            ));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo completar la búsqueda o guardar los resultados."));
        }
    }

    @GetMapping("/noticias")
    public ResponseEntity<List<Noticia>> listarNoticias() throws IOException {
        return ResponseEntity.ok(contenidoService.listarNoticias());
    }

    @GetMapping("/noticias/{id}")
    public ResponseEntity<Noticia> obtenerNoticia(@PathVariable String id) {
        try {
            return ResponseEntity.ok(contenidoService.obtenerNoticia(id));
        } catch (IOException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record SolicitudRecoleccion(String consulta, Integer limite) {}
}
