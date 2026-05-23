package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.UtilisateurResponse;
import com.example.gestion_financement.entity.Utilisateur;

import java.util.List;

public class UtilisateurMapper {

    private UtilisateurMapper() {}

    public static UtilisateurResponse toDto(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole().name())
                .actif(u.getActif())
                .dateCreation(u.getDateCreation())
                .build();
    }

    public static List<UtilisateurResponse> toDtoList(List<Utilisateur> list) {
        return list.stream().map(UtilisateurMapper::toDto).toList();
    }
}
