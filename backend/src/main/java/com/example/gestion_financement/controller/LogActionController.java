package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.LogActionMapper;
import com.example.gestion_financement.dto.response.LogActionResponse;
import com.example.gestion_financement.service.LogActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Logs", description = "Traçabilité des actions utilisateurs — ADMIN / RESPONSABLE")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogActionController {

    private final LogActionService logActionService;

    @Operation(summary = "100 dernières actions (tri décroissant)")
    @GetMapping
    public ResponseEntity<List<LogActionResponse>> findAll() {
        return ResponseEntity.ok(LogActionMapper.toDtoList(logActionService.findRecents()));
    }

    @Operation(summary = "Logs d'un utilisateur spécifique")
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<LogActionResponse>> findByUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(LogActionMapper.toDtoList(logActionService.findByUtilisateur(utilisateurId)));
    }

    @Operation(summary = "Logs d'une entité spécifique (ex : Escompte, Refinancement)")
    @GetMapping("/entite/{entite}")
    public ResponseEntity<List<LogActionResponse>> findByEntite(@PathVariable String entite) {
        return ResponseEntity.ok(LogActionMapper.toDtoList(logActionService.findByEntite(entite)));
    }

    @Operation(summary = "Logs sur une période donnée (format ISO : 2024-01-01T00:00:00)")
    @GetMapping("/periode")
    public ResponseEntity<List<LogActionResponse>> findByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(LogActionMapper.toDtoList(logActionService.findByPeriode(debut, fin)));
    }

    @Operation(summary = "Enregistrer une action manuellement")
    @PostMapping
    public ResponseEntity<LogActionResponse> log(
            @RequestParam Long utilisateurId,
            @RequestParam String action,
            @RequestParam(required = false) String entite,
            @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) String details) {
        return ResponseEntity.ok(
                LogActionMapper.toDto(logActionService.log(utilisateurId, action, entite, entiteId, details)));
    }
}
