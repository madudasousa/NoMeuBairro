package com.meubairro.api.dto.response;

import com.meubairro.api.domain.User.PerfilUser;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID id,
        String nome,
        PerfilUser perfil
) {
}
