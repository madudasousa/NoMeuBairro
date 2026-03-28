package com.meubairro.api.dto.response;

import java.util.UUID;

// Resposta resumida usada nos cards da home e na listagem
public record EstabResumeResponse(
        UUID id,
        String name,
        String descriptionCurta,
        String address,
        String category,
        String imageCapa
) {
}
