package com.meubairro.api.controller;

import com.meubairro.api.dto.request.ServiceRequest;
import com.meubairro.api.dto.response.ServiceResponse;
import com.meubairro.api.service.ServicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estabs/{estabId}/servicos")
@RequiredArgsConstructor
public class ServiceController {

    private final ServicesService servicesService;

    // Adiciona novos serviços ao estabelecimento
    @PostMapping
    public ResponseEntity<List<ServiceResponse>> salvar(
            @PathVariable UUID estabId,
            @RequestBody ServiceRequest request) {

        return ResponseEntity.ok(servicesService.salvar(estabId, request));
    }

    // Retorna todos os serviços do estabelecimento em ordem alfabética
    // Usado para montar as tags na tela de detalhes.
    @GetMapping
    public ResponseEntity<List<ServiceResponse>> listar(@PathVariable UUID estabId) {
        return ResponseEntity.ok(servicesService.listarPorEstab(estabId));
    }

    // Remove um serviço específico pelo id
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> remover(
            @PathVariable UUID estabId,
            @PathVariable UUID serviceId){

        servicesService.remover(estabId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<List<ServiceResponse>> substituir(
            @PathVariable UUID estabId,
            @RequestBody ServiceRequest request){

        return ResponseEntity.ok(servicesService.substituir(estabId, request));
    }
}
