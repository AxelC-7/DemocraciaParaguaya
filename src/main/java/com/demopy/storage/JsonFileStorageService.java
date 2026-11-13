package com.demopy.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class JsonFileStorageService {

    private final ObjectMapper objectMapper;
    private final Path basePath;

    public JsonFileStorageService(
            ObjectMapper objectMapper,
            @Value("${demopy.storage.path:./data}") String storagePath) {
        this.objectMapper = objectMapper;
        this.basePath = Path.of(storagePath).toAbsolutePath().normalize();
    }

    /**
     * Guarda un objeto como JSON. La escritura se realiza primero en un archivo
     * temporal y luego se reemplaza el destino para evitar dejar JSON incompletos.
     */
    public synchronized void guardar(String rutaRelativa, Object contenido) throws IOException {
        Path destino = resolverRuta(rutaRelativa);
        Path directorio = destino.getParent();

        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(
                directorio,
                "." + destino.getFileName() + "-",
                ".tmp"
        );

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporal.toFile(), contenido);
            try {
                Files.move(
                        temporal,
                        destino,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public <T> T leer(String rutaRelativa, Class<T> tipo) throws IOException {
        Path archivo = resolverRuta(rutaRelativa);
        if (!Files.isRegularFile(archivo)) {
            throw new IOException("No existe el archivo JSON: " + rutaRelativa);
        }
        return objectMapper.readValue(archivo.toFile(), tipo);
    }

    public <T> T leer(String rutaRelativa, TypeReference<T> tipo) throws IOException {
        Path archivo = resolverRuta(rutaRelativa);
        if (!Files.isRegularFile(archivo)) {
            throw new IOException("No existe el archivo JSON: " + rutaRelativa);
        }
        return objectMapper.readValue(archivo.toFile(), tipo);
    }

    public boolean existe(String rutaRelativa) {
        return Files.isRegularFile(resolverRuta(rutaRelativa));
    }

    private Path resolverRuta(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía.");
        }

        Path relativa = Path.of(rutaRelativa);
        if (relativa.isAbsolute()) {
            throw new IllegalArgumentException("Solo se permiten rutas relativas al directorio de datos.");
        }

        Path resuelta = basePath.resolve(relativa).normalize();
        if (!resuelta.startsWith(basePath) || resuelta.equals(basePath)) {
            throw new IllegalArgumentException("La ruta solicitada está fuera del directorio de datos.");
        }
        if (!resuelta.getFileName().toString().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("Solo se permiten archivos con extensión .json.");
        }

        return resuelta;
    }
}
