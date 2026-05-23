package com.example.gestion_financement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long utilisateurId;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private long expiresIn;
}
