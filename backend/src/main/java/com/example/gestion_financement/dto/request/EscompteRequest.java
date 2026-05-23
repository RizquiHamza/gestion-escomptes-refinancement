package com.example.gestion_financement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de création/modification d'un escompte.
 * Toutes les contraintes de validation Bean Validation sont déclarées ici
 * afin de ne jamais laisser de données invalides atteindre la couche service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données nécessaires pour créer ou modifier un escompte")
public class EscompteRequest {

    @NotNull(message = "L'identifiant du partenaire est obligatoire")
    @Positive(message = "L'identifiant du partenaire doit être positif")
    @Schema(description = "ID du partenaire (CLIENT)", example = "1")
    private Long partenaireId;

    @NotNull(message = "L'identifiant de la banque est obligatoire")
    @Positive(message = "L'identifiant de la banque doit être positif")
    @Schema(description = "ID de la banque finançant l'escompte", example = "1")
    private Long banqueId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être strictement positif")
    @Digits(integer = 13, fraction = 2,
            message = "Le montant doit avoir au maximum 13 chiffres entiers et 2 décimales")
    @Schema(description = "Montant de l'escompte en MAD", example = "100000.00")
    private BigDecimal montant;

    @NotNull(message = "Le taux est obligatoire")
    @Positive(message = "Le taux doit être strictement positif")
    @DecimalMax(value = "100.0", message = "Le taux ne peut pas dépasser 100 %")
    @Schema(description = "Taux d'escompte annuel en pourcentage", example = "5.00")
    private BigDecimal taux;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 1,   message = "La durée doit être d'au moins 1 jour")
    @Max(value = 365, message = "La durée ne peut pas dépasser 365 jours")
    @Schema(description = "Durée en jours", example = "30")
    private Integer duree;

    @FutureOrPresent(message = "La date d'échéance ne peut pas être dans le passé")
    @Schema(description = "Date d'échéance (optionnel)", example = "2026-06-30")
    private LocalDate dateEcheance;
}
