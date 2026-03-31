package com.meubairro.api.dto.response;

import java.util.List;
import java.util.UUID;

public record EstabResponse(
        UUID id,
        String name,
        String description,
        String address,
        String time,
        String phone,

        String linkWhatsapp,
        CategoryResponse category,
        List<ServiceResponse> services,
        List<ImageResponse> images,
        Boolean active
) {
}
