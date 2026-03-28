package com.meubairro.api.service;

import com.meubairro.api.domain.category.Category;
import com.meubairro.api.domain.estab.Estab;
import com.meubairro.api.dto.request.EstabCreateRequest;
import com.meubairro.api.dto.request.EstabUpdateRequest;
import com.meubairro.api.dto.request.FiltroEstabRequest;
import com.meubairro.api.dto.request.ServiceRequest;
import com.meubairro.api.dto.response.EstabResponse;
import com.meubairro.api.dto.response.EstabResumeResponse;
import com.meubairro.api.mapper.EstabMapper;
import com.meubairro.api.repositories.EstabRepository;
import com.meubairro.api.specification.EstabSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstabService {

    private final EstabRepository repository;
    private final CategoryService categoryService;
    private final ServicesService servicesService;
    private final EstabMapper mapper;

    @Transactional
    public EstabResponse createEstab(EstabCreateRequest request){
        if (repository.existsByNameIgnoreCase(request.name())){
            throw new RuntimeException("Já existe um estabelecimento com esse nome: " + request.name());
        }

        Category category = categoryService.buscarEntidadePorId(request.categoryId());

        Estab estab = Estab.builder()
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .time(request.time())
                .phone(request.phone())
                .category(category)
                .active(request.active() != null ? request.active() : true)
                .build();

        Estab salvo = repository.save(estab);

        //salvam os servicos se vieram no request
        if (request.services() != null && !request.services().isEmpty()){
        servicesService.salvar(salvo.getId(), new ServiceRequest(request.services()));
        }
        return mapper.toResponse(buscarEntidadePorId(salvo.getId()));
    }

    //busca os estabelecimentos ativos para a home com filtros e paginação
    public Page<EstabResumeResponse> listar(
            FiltroEstabRequest filtro, Pageable pageable){
        return repository
                .findAll(EstabSpecification.comfiltros(filtro),pageable)
                .map(mapper::toResume);
    }

    //buscar os detalhes completos de um estabelecimento por id
    public EstabResponse buscarPorId(UUID id){
        return mapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public EstabResponse editar(UUID id, EstabUpdateRequest request){
        Estab estab = buscarEntidadePorId(id);

        if (request.name() != null) estab.setName(request.name());
        if (request.description() != null) estab.setDescription(request.description());
        if (request.address() != null) estab.setAddress(request.address());
        if (request.time() != null) estab.setTime(request.time());
        if (request.phone() != null) estab.setPhone(request.phone());
        if (request.active() != null) estab.setActive(request.active());
        if (request.categoryId() != null){
            Category novacategory = categoryService.buscarEntidadePorId(request.categoryId());
            estab.setCategory(novacategory);
        }
        return mapper.toResponse(repository.save(estab));
    }

    @Transactional
    public void alterarStatus(UUID id, Boolean active){
        Estab estab = buscarEntidadePorId(id);
        estab.setActive(active);
        repository.save(estab);
    }

    @Transactional
    public void deletar(UUID id){
        if (!repository.existsById(id)){
            throw new EntityNotFoundException("Estabelecimento não encontrado");
        }
        repository.deleteById(id);
    }

    public Estab buscarEntidadePorId(UUID id){
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Estabelecimento não encontrado: " + id));
    }

    public Estab findById(UUID id) {
        return buscarEntidadePorId(id);
    }
}
