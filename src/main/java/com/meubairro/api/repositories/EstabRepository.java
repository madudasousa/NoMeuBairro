package com.meubairro.api.repositories;

import com.meubairro.api.domain.estab.Estab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EstabRepository extends JpaRepository <Estab, UUID> {
}
