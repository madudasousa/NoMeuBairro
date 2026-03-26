package com.meubairro.api.service;

import com.meubairro.api.domain.category.Category;
import com.meubairro.api.dto.request.CategoryRequest;
import com.meubairro.api.dto.response.CategoryResponse;
import com.meubairro.api.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse criar(CategoryRequest request) {
        if(request.name() == null || request.name().isBlank()){
            throw new RuntimeException("O nome da categoria não popde ser vazio");
        }

        //verifica duplicata pelo nome antes de salvar
        if (categoryRepository.existsByNomeIgnoreCase(request.name())) {
            throw new RuntimeException("Já existe uma categoria com o mesmo nome: " + request.name());
        }

        String slug = gerarSlug(request.name());

        //verifica duplicata pelo slug
        if (categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Já existe uma categoria com um nome muito similar: " + slug);
        }

        Category category = Category.builder()
                .name(request.name().trim())
                .slug(slug)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    //lista todas as categorias em ordem alfabetica
    public List<CategoryResponse> listar() {
        return categoryRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    //busca uma categoria pelo slug
    public CategoryResponse buscarPorSlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + slug));
    }

    //busca a enntidade categoria pelo id
    public Category buscarEntidadePorId(UUID id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada" ));
    }

    //Edita o nome de uma categoria existente eregenera o slug
    @Transactional
    public CategoryResponse editar(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        if (categoryRepository.existsByNomeIgnoreCase(request.name())
        && !category.getName().equalsIgnoreCase(request.name())){
            throw new RuntimeException("Já existe uma categoria com o mesmo nome: " + request.name());
        }

        category.setName(request.name().trim());
        category.setSlug(gerarSlug(request.name()));

        return toResponse(categoryRepository.save(category));
    }

    //remove uma categoria
    @Transactional
    public void deletar(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }
        categoryRepository.deleteById(id);
    }

    //gerar o slug apartir do nome
    private String gerarSlug(String name){
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFC)
                .replaceAll("[\\p{InCOMBINING_DIACRITICAL_MARKS}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    private CategoryResponse toResponse(Category category){
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug());
    }
}
