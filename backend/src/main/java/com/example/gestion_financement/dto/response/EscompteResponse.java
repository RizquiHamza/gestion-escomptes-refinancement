package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscompteResponse {
    private Long id;
    private String reference;
    private Long partenaireId;
    private String partenaireNom;
    private Long banqueId;
    private String banqueNom;
    private BigDecimal montant;
    private BigDecimal taux;
    private Integer duree;
    private LocalDate dateCreation;
    private LocalDate dateEcheance;
    private BigDecimal agios;
    private BigDecimal netRecu;
    private String statut;
}
