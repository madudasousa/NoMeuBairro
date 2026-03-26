package com.meubairro.api.dto.response;

import java.util.UUID;

public record ServiceResponse(UUID id, String name, UUID estabId) { }
