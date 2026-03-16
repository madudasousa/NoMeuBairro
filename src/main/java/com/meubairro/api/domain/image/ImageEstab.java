package com.meubairro.api.domain.image;

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
@Table (name= "image")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageEstab {
    @Id
    @GeneratedValue
    private UUID id;
    private String url;
    private String ordem;
    private String estab;
}
