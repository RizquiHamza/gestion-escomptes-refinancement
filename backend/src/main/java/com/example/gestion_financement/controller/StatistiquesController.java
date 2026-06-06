package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.response.StatistiquesResponse;
import com.example.gestion_financement.service.StatistiquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistiques", description = "Statistiques et rapports — ADMIN / RESPONSABLE")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @Operation(summary = "Statistiques complètes : compteurs, montants, par banque, évolution mensuelle")
    @ApiResponse(responseCode = "200", description = "Statistiques retournées")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<StatistiquesResponse> getStatistiques() {
        return ResponseEntity.ok(statistiquesService.getStatistiques());
    }
}
