package com.example.gestion_financement.entity;

import com.example.gestion_financement.enums.TypePartenaire;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "partenaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partenaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @Email
    @Column(unique = true)
    private String email;

    private String telephone;

    private String adresse;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePartenaire type;

    private LocalDate dateCreation;

    @JsonIgnore
    @OneToMany(mappedBy = "partenaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Escompte> escomptes;

    @JsonIgnore
    @OneToMany(mappedBy = "partenaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Refinancement> refinancements;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
    }
}
