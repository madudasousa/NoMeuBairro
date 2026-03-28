package com.meubairro.api.repositories;

import com.meubairro.api.domain.estab.Estab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Repository
public interface EstabRepository extends
        JpaRepository <Estab, UUID>,
        JpaSpecificationExecutor<Estab> {

    // Busca todos os estabelecimentos ativos com paginação.
    Page<Estab> findByActiveTrue(Pageable pageable);

    //verifica se ja existe um estabelecimento com o mesmo nome
    boolean existsByNameIgnoreCase(String name);
}
