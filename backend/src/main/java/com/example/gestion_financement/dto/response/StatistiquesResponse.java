package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesResponse {

    // ─── Escomptes ────────────────────────────────────────────────────────────────
    private long       totalEscomptes;
    private long       escomptesEnAttente;
    private long       escomptesApprouves;
    private long       escomptesRejetes;
    private long       escomptesAnnules;
    private long       escomptesClos;
    private BigDecimal montantTotalEscomptes;
    private BigDecimal agiosTotaux;
    private BigDecimal netRecuTotal;
    private double     tauxApprobationEscomptes;

    // ─── Refinancements ──────────────────────────────────────────────────────────
    private long       totalRefinancements;
    private long       refinancementsEnAttente;
    private long       refinancementsApprouves;
    private long       refinancementsRejetes;
    private long       refinancementsAnnules;
    private BigDecimal montantTotalRefinancements;
    private BigDecimal interetsTotaux;
    private BigDecimal totalRemboursement;
    private double     tauxApprobationRefinancements;

    // ─── Par banque (opérations approuvées) ──────────────────────────────────────
    private List<BanqueStatResponse> escomptesParBanque;
    private List<BanqueStatResponse> refinancementsParBanque;

    // ─── Évolution mensuelle (6 derniers mois) ───────────────────────────────────
    private List<MoisStatResponse> evolutionMensuelle;
}
