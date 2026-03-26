package com.meubairro.api.domain.category;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table (name = "categorys")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false, length = 100,  unique = true)
    private String name;

    // Slug é a versão do nome sem espaços e sem acentos, em minúsculo
    @Column(name = "name", nullable = false, length = 100,  unique = true)
    private String slug;
}
