package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogActionResponse {
    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String action;
    private String entite;
    private Long entiteId;
    private String details;
    private LocalDateTime dateAction;
}
