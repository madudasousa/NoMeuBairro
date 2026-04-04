package com.meubairro.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    private static final long TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024; // 5MB
    private static final List<String> CONTENT_TYPES_PERMITIDOS = List.of(
            "image/jpeg",
            "image/png"
    );

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Value("${upload.base-url:http://localhost:8080}")
    private String baseUrl;

    public String salvar(MultipartFile arquivo, UUID estabId) {
        validar(arquivo);

        try {
            Path diretorio = Paths.get(uploadDir, "estabelecimentos", estabId.toString());
            Files.createDirectories(diretorio);

            String extensao = extrairExtensao(arquivo.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path destino = diretorio.resolve(nomeArquivo);

            arquivo.transferTo(destino);

            return baseUrl + "/uploads/estabelecimentos/" + estabId + "/" + nomeArquivo;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem no servidor", e);
        }
    }

    public void deletar(String url) {
        try {
            String caminho = url.replace(baseUrl + "/", "");
            Path arquivo = Paths.get(caminho);
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar imagem do servidor", e);
        }
    }

    private void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Nenhum arquivo foi enviado");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new RuntimeException("O arquivo excede o tamanho máximo permitido de 5MB");
        }
        String contentType = arquivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new RuntimeException("Tipo de arquivo não permitido. Envie JPEG ou PNG");
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal != null && nomeOriginal.contains(".")) {
            return nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        }
        return ".jpg";
    }
}
