package com.meubairro.api.repositories;

import com.meubairro.api.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // Busca todas as categorias em ordem alfabética
    List<Category> findAllByOrderByNameAsc();

    // Busca por slug — usado quando o usuário clica em uma categoria na home
    Optional<Category> findBySlug(String slug);

    // Busca por nome ignorando maiúsculas e minúsculas
    Optional<Category> findByNameIgnoreCase(String name);

    // Verifica se já existe uma categoria com esse slug antes de salvar
    boolean existsBySlug(String slug);

    // Verifica se já existe uma categoria com esse nome antes de salvar
    boolean existsByNameIgnoreCase(String name);
}
