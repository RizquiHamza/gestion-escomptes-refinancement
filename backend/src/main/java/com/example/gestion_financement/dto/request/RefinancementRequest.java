package com.example.gestion_financement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefinancementRequest {

    @NotNull(message = "Le partenaire est obligatoire")
    private Long partenaireId;

    @NotNull(message = "La banque est obligatoire")
    private Long banqueId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull
    @DecimalMin(value = "0.01", message = "Le taux doit être positif")
    private BigDecimal taux;

    @NotNull
    @Min(value = 1, message = "La durée doit être au moins 1 an")
    private Integer duree;

    private LocalDate dateEcheance;
}
