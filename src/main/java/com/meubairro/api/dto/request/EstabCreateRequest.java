package com.meubairro.api.dto.request;


import java.util.List;
import java.util.UUID;

public record EstabCreateRequest(
        String name,
        String description,
        String address,
        String time,
        String phone,
        UUID categoryId,
        List<String> services,
        Boolean active) {

}
