package com.meubairro.api.repositories;

import com.meubairro.api.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
