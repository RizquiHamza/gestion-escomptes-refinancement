package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartenaireResponse {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String type;
    private LocalDate dateCreation;
}
