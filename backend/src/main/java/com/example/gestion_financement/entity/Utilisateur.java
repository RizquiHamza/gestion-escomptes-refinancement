package com.example.gestion_financement.entity;

import com.example.gestion_financement.enums.RoleUtilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @NotBlank
    @Column(nullable = false)
    private String prenom;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @NotNull
    @Convert(converter = com.example.gestion_financement.config.RoleUtilisateurConverter.class)
    @Column(nullable = false)
    private RoleUtilisateur role;

    @Builder.Default
    @Column(nullable = false)
    private Boolean actif = true;

    private LocalDate dateCreation;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<LogAction> logsActions;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
        if (actif == null) {
            actif = true;
        }
    }
}
