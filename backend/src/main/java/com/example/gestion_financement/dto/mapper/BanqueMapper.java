package com.example.gestion_financement.dto.mapper;

import com.example.gestion_financement.dto.response.BanqueResponse;
import com.example.gestion_financement.entity.Banque;

import java.util.List;

public class BanqueMapper {

    private BanqueMapper() {}

    public static BanqueResponse toDto(Banque b) {
        return BanqueResponse.builder()
                .id(b.getId())
                .nom(b.getNom())
                .code(b.getCode())
                .adresse(b.getAdresse())
                .telephone(b.getTelephone())
                .build();
    }

    public static List<BanqueResponse> toDtoList(List<Banque> list) {
        return list.stream().map(BanqueMapper::toDto).toList();
    }
}
