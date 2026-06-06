package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.response.DemandeReinitialisationResponse;
import com.example.gestion_financement.service.DemandeReinitialisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Demandes de réinitialisation", description = "Gestion des demandes de réinitialisation — ADMIN")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/demandes-reinitialisation")
@RequiredArgsConstructor
public class DemandeReinitialisationController {

    private final DemandeReinitialisationService demandeService;

    @Operation(summary = "Lister toutes les demandes (tri : plus récentes en premier)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeReinitialisationResponse>> findAll() {
        return ResponseEntity.ok(demandeService.findAll());
    }

    @Operation(summary = "Lister uniquement les demandes EN_ATTENTE")
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeReinitialisationResponse>> findEnAttente() {
        return ResponseEntity.ok(demandeService.findEnAttente());
    }

    @Operation(summary = "Approuver une demande — génère un mot de passe temporaire")
    @PostMapping("/{id}/approuver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> approuver(@PathVariable Long id) {
        String tempPassword = demandeService.approuver(id);
        return ResponseEntity.ok(Map.of(
            "motDePasseTemporaire", tempPassword,
            "message", "Demande approuvée. Communiquez ce mot de passe à l'utilisateur."
        ));
    }

    @Operation(summary = "Refuser une demande")
    @PostMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> refuser(@PathVariable Long id) {
        demandeService.refuser(id);
        return ResponseEntity.ok(Map.of("message", "Demande refusée."));
    }
}
