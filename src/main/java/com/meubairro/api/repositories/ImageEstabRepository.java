package com.meubairro.api.repositories;

import com.meubairro.api.domain.image.ImageEstab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageEstabRepository extends JpaRepository<ImageEstab, UUID> {
}
