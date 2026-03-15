package com.meubairro.api.domain.estab;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "estabs")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Estab {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String category;
    private String description;
    private String address;
    private String time;
    private String phone;
    private String image;
    private String services;


}
