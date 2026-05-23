package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.PartenaireResponse;
import com.example.gestion_financement.entity.Partenaire;

import java.util.List;

public class PartenaireMapper {

    private PartenaireMapper() {}

    public static PartenaireResponse toDto(Partenaire p) {
        return PartenaireResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .email(p.getEmail())
                .telephone(p.getTelephone())
                .adresse(p.getAdresse())
                .type(p.getType().name())
                .dateCreation(p.getDateCreation())
                .build();
    }

    public static List<PartenaireResponse> toDtoList(List<Partenaire> list) {
        return list.stream().map(PartenaireMapper::toDto).toList();
    }
}
