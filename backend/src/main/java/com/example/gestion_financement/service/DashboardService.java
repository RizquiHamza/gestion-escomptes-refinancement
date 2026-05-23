package com.example.gestion_financement.service;

import com.example.gestion_financement.dto.response.DashboardResponse;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EscompteRepository escompteRepository;
    private final RefinancementRepository refinancementRepository;
    private final PartenaireRepository partenaireRepository;
    private final BanqueRepository banqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DashboardResponse getDashboard() {
        BigDecimal montantEscomptes = escompteRepository.sumMontantApprouve();
        BigDecimal agios = escompteRepository.sumAgiosApprouve();
        BigDecimal montantRefinancements = refinancementRepository.sumMontantApprouve();
        BigDecimal interets = refinancementRepository.sumInteretsApprouve();

        return DashboardResponse.builder()
            // Escomptes
            .totalEscomptes(escompteRepository.count())
            .escomptesEnAttente(escompteRepository.countByStatut(StatutOperation.EN_ATTENTE))
            .escomptesApprouves(escompteRepository.countByStatut(StatutOperation.APPROUVE))
            .escomptesRejetes(escompteRepository.countByStatut(StatutOperation.REJETE))
            .escomptesClos(escompteRepository.countByStatut(StatutOperation.CLOS))
            .montantTotalEscomptes(montantEscomptes != null ? montantEscomptes : BigDecimal.ZERO)
            .agiosTotaux(agios != null ? agios : BigDecimal.ZERO)
            // Refinancements
            .totalRefinancements(refinancementRepository.count())
            .refinancementsEnAttente(refinancementRepository.countByStatut(StatutOperation.EN_ATTENTE))
            .refinancementsApprouves(refinancementRepository.countByStatut(StatutOperation.APPROUVE))
            .montantTotalRefinancements(montantRefinancements != null ? montantRefinancements : BigDecimal.ZERO)
            .interetsTotaux(interets != null ? interets : BigDecimal.ZERO)
            // Partenaires & autres
            .totalClients(partenaireRepository.countByType(TypePartenaire.CLIENT))
            .totalFournisseurs(partenaireRepository.countByType(TypePartenaire.FOURNISSEUR))
            .totalBanques(banqueRepository.count())
            .totalUtilisateurs(utilisateurRepository.count())
            .build();
    }
}
