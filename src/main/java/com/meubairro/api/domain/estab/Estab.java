package com.meubairro.api.domain.estab;

import com.meubairro.api.domain.image.ImageEstab;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
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
    private Integer phone;
    private String services;
    private Boolean active;
    private Long createAt;
    private String updateAt;

    @OneToMany(mappedBy = "estab", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ImageEstab> imagens = new ArrayList<>();
}
