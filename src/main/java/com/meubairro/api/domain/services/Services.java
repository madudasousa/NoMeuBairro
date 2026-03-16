package com.meubairro.api.domain.services;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table (name= "services")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Services {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String estab;
}
