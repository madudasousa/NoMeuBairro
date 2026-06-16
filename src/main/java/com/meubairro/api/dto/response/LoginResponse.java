package com.meubairro.api.dto.response;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID id,
        String nome,
        String perfil
) {
}
