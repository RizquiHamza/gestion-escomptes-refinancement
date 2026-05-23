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
public class DashboardResponse {

    // Escomptes
    private long totalEscomptes;
    private long escomptesEnAttente;
    private long escomptesApprouves;
    private long escomptesRejetes;
    private long escomptesClos;
    private BigDecimal montantTotalEscomptes;
    private BigDecimal agiosTotaux;

    // Refinancements
    private long totalRefinancements;
    private long refinancementsEnAttente;
    private long refinancementsApprouves;
    private BigDecimal montantTotalRefinancements;
    private BigDecimal interetsTotaux;

    // Partenaires & Banques
    private long totalClients;
    private long totalFournisseurs;
    private long totalBanques;
    private long totalUtilisateurs;
}
