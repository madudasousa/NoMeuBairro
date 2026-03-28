package com.meubairro.api.dto.request;

import java.util.UUID;

public record EstabUpdateRequest(
        String name,
        String description,
        String address,
        String time,
        String phone,
        UUID categoryId,
        Boolean active
) { }
