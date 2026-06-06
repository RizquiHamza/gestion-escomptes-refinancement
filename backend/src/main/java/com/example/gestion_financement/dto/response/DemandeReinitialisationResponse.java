package com.example.gestion_financement.dto.response;

import com.example.gestion_financement.enums.StatutDemande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeReinitialisationResponse {
    private Long           id;
    private Long           utilisateurId;
    private String         utilisateurNom;
    private String         utilisateurPrenom;
    private String         utilisateurEmail;
    private String         utilisateurRole;
    private StatutDemande  statut;
    private String         message;
    private LocalDateTime  dateCreation;
    private LocalDateTime  dateTraitement;
}
