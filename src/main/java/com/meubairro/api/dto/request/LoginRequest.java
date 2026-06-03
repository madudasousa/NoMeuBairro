package com.meubairro.api.dto.request;

public record LoginRequest(
        String documento, // CPF ou CNPJ sem formatacao
        String senha
) { }

