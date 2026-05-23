package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanqueResponse {
    private Long id;
    private String nom;
    private String code;
    private String adresse;
    private String telephone;
}
