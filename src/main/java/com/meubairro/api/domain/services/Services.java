package com.meubairro.api.domain.services;

import com.meubairro.api.domain.estab.Estab;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table (name= "services")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Services {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "estab_id")
    private Estab estab;
}
