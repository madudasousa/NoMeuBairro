package com.meubairro.api.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "estabs")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estab {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(length = 250)
    private String address;
    @Column(length = 100)
    private String time;
    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "estab", cascade = CascadeType.ALL, orphanRemoval = true)

    @OrderBy("name ASC")
    @Builder.Default
    private List<Services> services = new ArrayList<>();

    @OneToMany(mappedBy = "estab", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    @Builder.Default
    private List<ImageEstab> images = new ArrayList<>();

    @Column(name = "active_owner", nullable = false)
    @Builder.Default
    private Boolean activeOwner = true;

    @Column(name = "active_admin", nullable = false)
    @Builder.Default
    private Boolean activeAdmin = true;

    @CreationTimestamp
    @Column(name = "createAt", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updateAt")
    private LocalDateTime updateAt;

    // loja só aparece na home se AMBOS forem true
    public boolean isVisivelNaHome() {
        return Boolean.TRUE.equals(activeOwner) && Boolean.TRUE.equals(activeAdmin);
    }
}
