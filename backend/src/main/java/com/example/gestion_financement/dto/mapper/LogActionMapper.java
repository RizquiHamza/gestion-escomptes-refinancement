package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.LogActionResponse;
import com.example.gestion_financement.entity.LogAction;

import java.util.List;

public class LogActionMapper {

    private LogActionMapper() {}

    public static LogActionResponse toDto(LogAction l) {
        return LogActionResponse.builder()
                .id(l.getId())
                .utilisateurId(l.getUtilisateur() != null ? l.getUtilisateur().getId() : null)
                .utilisateurNom(l.getUtilisateur() != null ? l.getUtilisateur().getNom() : null)
                .utilisateurPrenom(l.getUtilisateur() != null ? l.getUtilisateur().getPrenom() : null)
                .action(l.getAction())
                .entite(l.getEntite())
                .entiteId(l.getEntiteId())
                .details(l.getDetails())
                .dateAction(l.getDateAction())
                .build();
    }

    public static List<LogActionResponse> toDtoList(List<LogAction> list) {
        return list.stream().map(LogActionMapper::toDto).toList();
    }
}
