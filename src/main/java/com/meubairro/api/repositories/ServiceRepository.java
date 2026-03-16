package com.meubairro.api.repositories;

import com.meubairro.api.domain.services.Services;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Services, UUID> {
}
