package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.RefinancementResponse;
import com.example.gestion_financement.entity.Refinancement;

import java.util.List;

public class RefinancementMapper {

    private RefinancementMapper() {}

    public static RefinancementResponse toDto(Refinancement r) {
        return RefinancementResponse.builder()
                .id(r.getId())
                .reference(r.getReference())
                .partenaireId(r.getPartenaire() != null ? r.getPartenaire().getId() : null)
                .partenaireNom(r.getPartenaire() != null ? r.getPartenaire().getNom() : null)
                .banqueId(r.getBanque() != null ? r.getBanque().getId() : null)
                .banqueNom(r.getBanque() != null ? r.getBanque().getNom() : null)
                .montant(r.getMontant())
                .taux(r.getTaux())
                .duree(r.getDuree())
                .dateCreation(r.getDateCreation())
                .dateEcheance(r.getDateEcheance())
                .interets(r.getInterets())
                .totalRemboursement(r.getTotalRemboursement())
                .statut(r.getStatut() != null ? r.getStatut().name() : null)
                .build();
    }

    public static List<RefinancementResponse> toDtoList(List<Refinancement> list) {
        return list.stream().map(RefinancementMapper::toDto).toList();
    }
}
