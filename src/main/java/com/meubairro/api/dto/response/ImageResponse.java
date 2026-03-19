package com.meubairro.api.dto.response;

import java.util.UUID;

public record ImageResponse(UUID id,
                            UUID estabId,
                            String url,
                            String nomeArquivo,
                            String contentType,
                            Integer ordem) { }
