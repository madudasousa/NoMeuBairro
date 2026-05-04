package com.meubairro.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImageController {
    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> servirImagem(HttpServletRequest request) throws IOException {
        String caminho = request.getRequestURI().substring("/uploads/".length());

        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(caminho).normalize();

        // Impede acesso fora da pasta de uploads (path traversal).
        if (!filePath.startsWith(basePath)) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}