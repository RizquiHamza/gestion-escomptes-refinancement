package com.example.gestion_financement.entity;

import com.example.gestion_financement.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "demande_reinitialisation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeReinitialisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateTraitement;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
