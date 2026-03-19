package com.meubairro.api.domain.image;

import com.meubairro.api.domain.estab.Estab;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table (name= "image")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEstab {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estab estab;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "nome_arquivo", length = 255)
    private String nomeArquivo;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;
}
