package com.example.gestion_financement.service;

import com.example.gestion_financement.dto.response.BanqueStatResponse;
import com.example.gestion_financement.dto.response.MoisStatResponse;
import com.example.gestion_financement.dto.response.StatistiquesResponse;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.repository.EscompteRepository;
import com.example.gestion_financement.repository.RefinancementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiquesService {

    private final EscompteRepository      escompteRepository;
    private final RefinancementRepository refinancementRepository;

    public StatistiquesResponse getStatistiques() {

        // ─── Compteurs Escomptes ──────────────────────────────────────────────────
        long totalE  = escompteRepository.count();
        long attE    = escompteRepository.countByStatut(StatutOperation.EN_ATTENTE);
        long appE    = escompteRepository.countByStatut(StatutOperation.APPROUVE);
        long rejE    = escompteRepository.countByStatut(StatutOperation.REJETE);
        long annE    = escompteRepository.countByStatut(StatutOperation.ANNULE);
        long closE   = escompteRepository.countByStatut(StatutOperation.CLOS);
        BigDecimal mtE = orZero(escompteRepository.sumMontantApprouve());
        BigDecimal agE = orZero(escompteRepository.sumAgiosApprouve());
        BigDecimal nrE = orZero(escompteRepository.sumNetRecuApprouve());
        double tauxAppE = totalE > 0 ? Math.round((double) appE / totalE * 1000.0) / 10.0 : 0;

        // ─── Compteurs Refinancements ─────────────────────────────────────────────
        long totalR  = refinancementRepository.count();
        long attR    = refinancementRepository.countByStatut(StatutOperation.EN_ATTENTE);
        long appR    = refinancementRepository.countByStatut(StatutOperation.APPROUVE);
        long rejR    = refinancementRepository.countByStatut(StatutOperation.REJETE);
        long annR    = refinancementRepository.countByStatut(StatutOperation.ANNULE);
        BigDecimal mtR = orZero(refinancementRepository.sumMontantApprouve());
        BigDecimal inR = orZero(refinancementRepository.sumInteretsApprouve());
        BigDecimal trR = orZero(refinancementRepository.sumTotalRemboursementApprouve());
        double tauxAppR = totalR > 0 ? Math.round((double) appR / totalR * 1000.0) / 10.0 : 0;

        // ─── Stats par banque ─────────────────────────────────────────────────────
        List<BanqueStatResponse> escomptesParBanque = escompteRepository.statsParBanque()
            .stream()
            .map(row -> BanqueStatResponse.builder()
                .banqueNom((String) row[0])
                .count(((Number) row[1]).longValue())
                .montantTotal(toBD(row[2]))
                .chargesTotal(toBD(row[3]))
                .build())
            .toList();

        List<BanqueStatResponse> refinancementsParBanque = refinancementRepository.statsParBanque()
            .stream()
            .map(row -> BanqueStatResponse.builder()
                .banqueNom((String) row[0])
                .count(((Number) row[1]).longValue())
                .montantTotal(toBD(row[2]))
                .chargesTotal(toBD(row[3]))
                .build())
            .toList();

        // ─── Évolution mensuelle (6 derniers mois) ───────────────────────────────
        LocalDate depuis = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        Map<String, long[]>       cntMapE = new HashMap<>();
        Map<String, BigDecimal[]> mtMapE  = new HashMap<>();
        for (Object[] row : escompteRepository.evolutionMensuelle(depuis)) {
            String key = row[0] + "-" + row[1];
            cntMapE.put(key, new long[]{ ((Number) row[2]).longValue() });
            mtMapE.put(key,  new BigDecimal[]{ toBD(row[3]) });
        }

        Map<String, long[]>       cntMapR = new HashMap<>();
        Map<String, BigDecimal[]> mtMapR  = new HashMap<>();
        for (Object[] row : refinancementRepository.evolutionMensuelle(depuis)) {
            String key = row[0] + "-" + row[1];
            cntMapR.put(key, new long[]{ ((Number) row[2]).longValue() });
            mtMapR.put(key,  new BigDecimal[]{ toBD(row[3]) });
        }

        List<MoisStatResponse> evolution = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate d     = LocalDate.now().minusMonths(i);
            int annee       = d.getYear();
            int mois        = d.getMonthValue();
            String key      = annee + "-" + mois;
            String label    = Month.of(mois).getDisplayName(TextStyle.SHORT, Locale.FRENCH);

            evolution.add(MoisStatResponse.builder()
                .annee(annee)
                .mois(mois)
                .label(capitalize(label))
                .countEscomptes(cntMapE.containsKey(key) ? cntMapE.get(key)[0] : 0L)
                .countRefinancements(cntMapR.containsKey(key) ? cntMapR.get(key)[0] : 0L)
                .montantEscomptes(mtMapE.containsKey(key) ? mtMapE.get(key)[0] : BigDecimal.ZERO)
                .montantRefinancements(mtMapR.containsKey(key) ? mtMapR.get(key)[0] : BigDecimal.ZERO)
                .build());
        }

        return StatistiquesResponse.builder()
            .totalEscomptes(totalE)
            .escomptesEnAttente(attE)
            .escomptesApprouves(appE)
            .escomptesRejetes(rejE)
            .escomptesAnnules(annE)
            .escomptesClos(closE)
            .montantTotalEscomptes(mtE)
            .agiosTotaux(agE)
            .netRecuTotal(nrE)
            .tauxApprobationEscomptes(tauxAppE)
            .totalRefinancements(totalR)
            .refinancementsEnAttente(attR)
            .refinancementsApprouves(appR)
            .refinancementsRejetes(rejR)
            .refinancementsAnnules(annR)
            .montantTotalRefinancements(mtR)
            .interetsTotaux(inR)
            .totalRemboursement(trR)
            .tauxApprobationRefinancements(tauxAppR)
            .escomptesParBanque(escomptesParBanque)
            .refinancementsParBanque(refinancementsParBanque)
            .evolutionMensuelle(evolution)
            .build();
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal toBD(Object o) {
        return o != null ? new BigDecimal(o.toString()) : BigDecimal.ZERO;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
