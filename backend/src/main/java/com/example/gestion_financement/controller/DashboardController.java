package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.response.DashboardResponse;
import com.example.gestion_financement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dashboard", description = "Statistiques globales — ADMIN / RESPONSABLE")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Statistiques globales : compteurs et montants agrégés")
    @ApiResponse(responseCode = "200", description = "Dashboard retourné")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
