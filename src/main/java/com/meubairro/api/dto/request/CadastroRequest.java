package com.meubairro.api.dto.request;

import com.meubairro.api.domain.PerfilUser;

public record CadastroRequest(
        String nome,
        String documento,
        String senha,
        PerfilUser perfil
) { }
