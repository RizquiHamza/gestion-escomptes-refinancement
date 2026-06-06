package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoisStatResponse {
    private int        annee;
    private int        mois;
    private String     label;
    private long       countEscomptes;
    private long       countRefinancements;
    private BigDecimal montantEscomptes;
    private BigDecimal montantRefinancements;
}
