package com.example.gestion_financement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "banque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nom;

    @Column(unique = true)
    private String code;

    private String adresse;

    private String telephone;

    @JsonIgnore
    @OneToMany(mappedBy = "banque", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Escompte> escomptes;

    @JsonIgnore
    @OneToMany(mappedBy = "banque", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Refinancement> refinancements;
}
