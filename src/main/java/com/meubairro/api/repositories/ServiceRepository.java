package com.meubairro.api.repositories;

import com.meubairro.api.domain.services.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Services, UUID> {
    List<Services> findByEstabIdOrderByNameAsc(UUID estabId);

    //verifica se um serviço ja existe no estab com esse nome
    boolean existsByEstabIdAndNameIgnoreCase(UUID estabId, String name);

    //deleta todos os serviços do estabelecimento de uma vez
    @Modifying
    @Query("DELETE FROM Services s WHERE s.estab.id = :estabId")
    void deleteAllByEstabId(@Param("estabId") UUID estabId);
}
