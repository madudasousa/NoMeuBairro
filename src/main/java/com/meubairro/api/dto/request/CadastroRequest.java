package com.meubairro.api.dto.request;

import com.meubairro.api.domain.User.PerfilUser;

public record CadastroRequest(
        String nome,
        String documento,
        String senha,
        PerfilUser perfil
) { }
