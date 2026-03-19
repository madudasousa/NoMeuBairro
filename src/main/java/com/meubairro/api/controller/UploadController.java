package com.meubairro.api.controller;

import com.meubairro.api.dto.request.ImageRequest;
import com.meubairro.api.dto.response.ImageResponse;
import com.meubairro.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estabs/{estabId}/imagens")
@RequiredArgsConstructor
public class UploadController {

    private final ImageService imagemService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageResponse>> upload(
            @PathVariable UUID estabId,
            @RequestParam("arquivos") List<MultipartFile> arquivos) {

        ImageRequest request = new ImageRequest(arquivos);
        return ResponseEntity.ok(imagemService.salvar(estabId, request));
    }

    @GetMapping
    public ResponseEntity<List<ImageResponse>> listar(@PathVariable UUID estabId) {
        return ResponseEntity.ok(imagemService.listarPorEstab(estabId));
    }

    @DeleteMapping("/{imagemId}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID estabId,
            @PathVariable UUID imagemId) {

        imagemService.deletar(estabId, imagemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reordenar")
    public ResponseEntity<List<ImageResponse>> reordenar(
            @PathVariable UUID estabId,
            @RequestBody List<UUID> idsNaOrdem) {

        return ResponseEntity.ok(imagemService.reordenarImagens(estabId, idsNaOrdem));
    }

}

