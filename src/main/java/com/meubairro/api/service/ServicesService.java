package com.meubairro.api.service;

import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.domain.services.Services;
import com.meubairro.api.dto.request.ServiceRequest;
import com.meubairro.api.dto.response.ServiceResponse;
import com.meubairro.api.repositories.EstabRepository;
import com.meubairro.api.repositories.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicesService {

    // Limite de serviços por estabelecimento para não poluir a tela de detalhes
    private static final int LIMITE_SERVICOS = 20;

    private final ServiceRepository serviceRepository;
    private final EstabRepository estabRepository;

    //salva uma lista de serviços para um estabelecimento
    @Transactional
    public List<ServiceResponse> salvar(UUID estabId, ServiceRequest request) {
        Estab estab = buscarEstabPorId(estabId);

        long totalAtual = serviceRepository.findByEstabIdOrderByNameAsc(estabId).size();

        if (totalAtual + request.names().size() > LIMITE_SERVICOS) {
            throw new IllegalArgumentException("Limite de serviços excedido. Máximo permitido: " + LIMITE_SERVICOS);
        }

        List<Services> novos = request.names().stream().filter(name -> name != null && !name.isBlank())
                .filter(name -> !serviceRepository.existsByEstabIdAndNameIgnoreCase(estabId, name))
                .map(name -> Services.builder().name(name.trim()).estab(estab).build())
                .toList();

        List<Services> salvos = serviceRepository.saveAll(novos);
        return salvos.stream().map(this::toResponse).toList();
    }

    //lista todos os serviços de um estabelecimento em ordem alfabetica
    public List<ServiceResponse> listarPorEstab(UUID estabId) {
        buscarEstabPorId(estabId);
        return serviceRepository.findByEstabIdOrderByNameAsc(estabId).stream().map(this::toResponse).toList();
    }
    //// Remove um serviço específico
    //Valida se o serviço realmente pertence ao estabelecimento informado
    @Transactional
    public void remover(UUID estabId, UUID serviceId) {
        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));
        if (!service.getEstab().getId().equals(estabId)) {
            throw new IllegalArgumentException("O serviço não pertence ao estabelecimento informado");
        }
        serviceRepository.delete(service);
    }

    // Substitui toda a lista de serviços de um estabelecimento
    @Transactional
    public List<ServiceResponse> substituir(UUID estabId, ServiceRequest request) {
        buscarEstabPorId(estabId);
        serviceRepository.deleteAllByEstabId(estabId);

        // Reutiliza o método salvar com a lista nova
        return salvar(estabId, request);
    }

    // Converte a entidade Servico para o DTO de resposta
    private ServiceResponse toResponse(Services service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getEstab().getId());
    }

    private Estab buscarEstabPorId(UUID estabId) {
        return estabRepository.findById(estabId)
                .orElseThrow(() -> new EntityNotFoundException("Estabelecimento não encontrado: " + estabId));
    }
}
