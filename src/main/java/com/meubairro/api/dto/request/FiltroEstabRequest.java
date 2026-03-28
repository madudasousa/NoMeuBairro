package com.meubairro.api.dto.request;

public record FiltroEstabRequest(
        String name,
        String categorySlug,
        String address
) {}
