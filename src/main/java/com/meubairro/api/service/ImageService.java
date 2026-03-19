package com.meubairro.api.service;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.domain.image.ImageEstab;
import com.meubairro.api.dto.request.ImageRequest;
import com.meubairro.api.dto.response.ImageResponse;
import com.meubairro.api.repositories.ImageEstabRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private static final int LIMITE_IMAGENS = 10;

    private final ImageEstabRepository imagemRepository;
    private final EstabService estabService;
    private final UploadService uploadService;

    @Transactional
    public List<ImageResponse> salvar(UUID estabId, ImageRequest request) {
        Estab estab = estabService.findById(estabId);

        long totalAtual = imagemRepository.countByEstabId(estabId);
        int totalEnviadas = request.arquivos().size();

        if (totalAtual + totalEnviadas > LIMITE_IMAGENS) {
            throw new RuntimeException(
                    "Limite de " + LIMITE_IMAGENS + " imagens por estabelecimento atingido. " +
                            "Você já possui " + totalAtual + " imagem(ns)."
            );
        }

        List<ImageResponse> salvas = new ArrayList<>();
        int proximaOrdem = (int) totalAtual;

        for (MultipartFile arquivo : request.arquivos()) {
            String url = uploadService.salvar(arquivo, estabId);

            ImageEstab imagem = ImageEstab.builder()
                    .estab(estab)
                    .url(url)
                    .nomeArquivo(arquivo.getOriginalFilename())
                    .contentType(arquivo.getContentType())
                    .ordem(proximaOrdem++)
                    .build();

            ImageEstab salva = imagemRepository.save(imagem);
            salvas.add(toResponse(salva));
        }
        return salvas;
    }

    public List<ImageResponse> listarPorEstab(UUID estabId) {
        estabService.findById(estabId);
        return imagemRepository.findByEstabIdOrderByOrdemAsc(estabId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deletar(UUID estabId, UUID imagemId) {
        ImageEstab imagem = imagemRepository.findById(imagemId)
                .orElseThrow(() -> new EntityNotFoundException("Imagem não encontrada"));

        if (!imagem.getEstab().getId().equals(estabId)) {
            throw new RuntimeException("Esta imagem não pertence ao estabelecimento informado");
        }

        uploadService.deletar(imagem.getUrl());
        imagemRepository.delete(imagem);

        reordenar(estabId);
    }

    @Transactional
    public List<ImageResponse> reordenarImagens(UUID estabId, List<UUID> idsNaOrdem) {
        estabService.findById(estabId);

        List<ImageEstab> imagens = imagemRepository.findByEstabIdOrderByOrdemAsc(estabId);

        for (int i = 0; i < idsNaOrdem.size(); i++) {
            UUID idImagem = idsNaOrdem.get(i);
            int novaOrdem = i;

            imagens.stream()
                    .filter(img -> img.getId().equals(idImagem))
                    .findFirst()
                    .ifPresent(img -> img.setOrdem(novaOrdem));
        }

        imagemRepository.saveAll(imagens);
        return imagens.stream().map(this::toResponse).toList();
    }

    private void reordenar(UUID estabId) {
        List<ImageEstab> imagens = imagemRepository.findByEstabIdOrderByOrdemAsc(estabId);
        for (int i = 0; i < imagens.size(); i++) {
            imagens.get(i).setOrdem(i);
        }
        imagemRepository.saveAll(imagens);
    }

    private ImageResponse toResponse(ImageEstab imagem) {
        return new ImageResponse(
                imagem.getId(),
                imagem.getEstab().getId(),
                imagem.getUrl(),
                imagem.getNomeArquivo(),
                imagem.getContentType(),
                imagem.getOrdem()
        );
    }
}
