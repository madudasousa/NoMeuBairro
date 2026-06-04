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
import com.meubairro.api.repositories.UserRepository;
import com.meubairro.api.specification.EstabSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstabService {

    private final EstabRepository repository;
    private final CategoryService categoryService;
    private final ServicesService servicesService;
    private final EstabMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EstabResponse createEstab(EstabCreateRequest request){
        if (repository.existsByNameIgnoreCase(request.name())){
            throw new RuntimeException("Já existe um estabelecimento com esse nome: " + request.name());
        }
        String document = limparDocumento(request.document());
        validarDocumento(document);

        if (userRepository.existsByDocument(document)){
            throw new RuntimeException("Já existe uma conta com esse CPF/CNPJ");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw new RuntimeException("A senha deve conter pelo menos 6 caracteres.");
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
                .services(new ArrayList<>())
                .images(new ArrayList<>())
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

    private String limparDocumento(String document){
        return (document == null ? "" : document).replaceAll("[^0-9]", "");
    }

    private void validarDocumento(String document){
        if (document.length() == 11){
            if (!validarCpf(document)){
                throw new RuntimeException("CPF inválido.");
            }
        } else if (document.length() == 14) {
            if (!validarCnpj(document)){
                throw new RuntimeException("CNPJ inválido.");
            }
        }else {
            throw new RuntimeException("Documento inválido. Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).");
        }
    }

    private boolean validarCpf(String cpf) {
        if (cpf.chars().distinct().count() == 1) return false;

        int[] n = cpf.chars().map(c -> c - '0').toArray();

        // Primeiro dígito
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += n[i] * (10 - i);
        int resto = soma % 11;
        int primeiro = resto < 2 ? 0 : 11 - resto;
        if (primeiro != n[9]) return false;

        // Segundo dígito
        soma = 0;
        for (int i = 0; i < 10; i++) soma += n[i] * (11 - i);
        resto = soma % 11;
        int segundo = resto < 2 ? 0 : 11 - resto;
        return segundo == n[10];
    }

    private boolean validarCnpj(String cnpj){
        if (cnpj.chars().distinct().count() == 1) return false;
        int[] n = cnpj.chars().map(c -> c - '0').toArray();
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += n[i] * p1[i];
        int primeiro = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (primeiro != n[12]) return false;

        soma = 0;
        for (int i = 0; i < 13; i++) soma += n[i] * p2[i];
        int segundo = soma % 11 < 2 ? 0 : 11 - soma % 11;
        return segundo == n[13];
    }
}
