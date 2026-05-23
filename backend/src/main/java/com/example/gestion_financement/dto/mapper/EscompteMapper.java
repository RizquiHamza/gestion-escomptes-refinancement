package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.EscompteResponse;
import com.example.gestion_financement.entity.Escompte;

import java.util.List;

public class EscompteMapper {

    private EscompteMapper() {}

    public static EscompteResponse toDto(Escompte e) {
        return EscompteResponse.builder()
                .id(e.getId())
                .reference(e.getReference())
                .partenaireId(e.getPartenaire() != null ? e.getPartenaire().getId() : null)
                .partenaireNom(e.getPartenaire() != null ? e.getPartenaire().getNom() : null)
                .banqueId(e.getBanque() != null ? e.getBanque().getId() : null)
                .banqueNom(e.getBanque() != null ? e.getBanque().getNom() : null)
                .montant(e.getMontant())
                .taux(e.getTaux())
                .duree(e.getDuree())
                .dateCreation(e.getDateCreation())
                .dateEcheance(e.getDateEcheance())
                .agios(e.getAgios())
                .netRecu(e.getNetRecu())
                .statut(e.getStatut() != null ? e.getStatut().name() : null)
                .build();
    }

    public static List<EscompteResponse> toDtoList(List<Escompte> list) {
        return list.stream().map(EscompteMapper::toDto).toList();
    }
}
