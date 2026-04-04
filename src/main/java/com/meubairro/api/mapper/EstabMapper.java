package com.meubairro.api.mapper;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.domain.services.Services;
import com.meubairro.api.util.WhatsappUtil;
import com.meubairro.api.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class EstabMapper {

    //converte para resumo dos cards da home
    public EstabResumeResponse toResume(Estab e){
        String descriptionCurta = null;
        if (e.getDescription() != null && !e.getDescription().isBlank()){
            descriptionCurta = e.getDescription().length() > 100
            ? e.getDescription().substring(0, 100) + "..." : e.getDescription();
        }

        var imagesList = e.getImages() != null ? e.getImages() : Collections.<com.meubairro.api.domain.image.ImageEstab>emptyList();
        String imageCapa = imagesList.isEmpty() ? null : imagesList.get(0).getUrl();

        String nameCategory = e.getCategory() != null ? e.getCategory().getName() : null;

        return new EstabResumeResponse(
                e.getId(),
                e.getName(),
                descriptionCurta,
                e.getAddress(),
                nameCategory,
                imageCapa
        );
    }

    // Converte para o response completo da tela de detalhes
    // Monta o link do WhatsApp, converte serviços e imagens para DTOs
    public EstabResponse toResponse(Estab e){
        CategoryResponse categoryResponse = null;
        if (e.getCategory() != null){
            categoryResponse = new CategoryResponse(
                    e.getCategory().getId(),
                    e.getCategory().getName(),
                    e.getCategory().getSlug()
            );
        }

        var servicesList = e.getServices() != null ? e.getServices() : new ArrayList<Services>();
        var service = servicesList.stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), e.getId()))
                .toList();

        var imagesList = e.getImages() != null ? e.getImages() : Collections.<com.meubairro.api.domain.image.ImageEstab>emptyList();
        var images = imagesList.stream()
                .map(i -> new ImageResponse(
                        i.getId(), e.getId(), i.getUrl(), i.getNomeArquivo(), i.getContentType(), i.getOrdem()
                )).toList();

        return new EstabResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getAddress(),
                e.getTime(),
                e.getPhone(),
                WhatsappUtil.gerarLink(e.getPhone()),
                categoryResponse,
                service,
                images,
                e.getActive()
        );
    }
}
