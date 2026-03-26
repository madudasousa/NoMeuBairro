package com.meubairro.api.dto.response;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String slug) { }
