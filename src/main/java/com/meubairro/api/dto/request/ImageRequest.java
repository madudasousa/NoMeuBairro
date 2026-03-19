package com.meubairro.api.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ImageRequest(List<MultipartFile> arquivos) {
}
