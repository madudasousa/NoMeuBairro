package com.meubairro.api.repositories;

import com.meubairro.api.domain.image.ImageEstab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageEstabRepository extends JpaRepository<ImageEstab, UUID> {
    List<ImageEstab> findByEstabIdOrderByOrdemAsc(UUID estabId);

    long countByEstabId(UUID estabId);

    @Modifying
    @Query("DELETE FROM ImageEstab i WHERE i.estab.id = :estabId")
    void deleteAllByEstabId(@Param("estabId") UUID estabId);
}
