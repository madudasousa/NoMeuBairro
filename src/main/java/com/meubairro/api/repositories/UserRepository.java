package com.meubairro.api.repositories;

import com.meubairro.api.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByDocument(String document);
    boolean existsByDocument(String document);
}
