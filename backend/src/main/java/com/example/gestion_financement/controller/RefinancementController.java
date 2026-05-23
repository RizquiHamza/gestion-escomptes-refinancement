package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.RefinancementMapper;
import com.example.gestion_financement.dto.request.RefinancementRequest;
import com.example.gestion_financement.dto.response.RefinancementResponse;
import com.example.gestion_financement.entity.Refinancement;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.service.RefinancementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Refinancements", description = "Gestion des opérations de refinancement (fournisseurs)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/refinancements")
@RequiredArgsConstructor
public class RefinancementController {

    private final RefinancementService refinancementService;

    @Operation(summary = "Liste paginée des refinancements",
               description = "Filtres optionnels : statut (EN_ATTENTE, APPROUVE, REJETE, CLOS) ou partenaireId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de refinancements retournée"),
        @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<Page<RefinancementResponse>> findAll(
            @Parameter(description = "Filtrer par statut") @RequestParam(required = false) StatutOperation statut,
            @Parameter(description = "Filtrer par ID partenaire") @RequestParam(required = false) Long partenaireId,
            @PageableDefault(size = 10, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Refinancement> page;
        if (statut != null)           page = refinancementService.findByStatut(statut, pageable);
        else if (partenaireId != null) page = refinancementService.findByPartenaire(partenaireId, pageable);
        else                           page = refinancementService.findAll(pageable);
        return ResponseEntity.ok(page.map(RefinancementMapper::toDto));
    }

    @Operation(summary = "Détail d'un refinancement par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Refinancement trouvé"),
        @ApiResponse(responseCode = "404", description = "Refinancement introuvable")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<RefinancementResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(RefinancementMapper.toDto(refinancementService.findById(id)));
    }

    @Operation(summary = "Créer un nouveau refinancement")
    @ApiResponse(responseCode = "201", description = "Refinancement créé")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<RefinancementResponse> create(@Valid @RequestBody RefinancementRequest request) {
        Refinancement refinancement = Refinancement.builder()
                .montant(request.getMontant())
                .taux(request.getTaux())
                .duree(request.getDuree())
                .dateEcheance(request.getDateEcheance())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RefinancementMapper.toDto(
                        refinancementService.create(request.getPartenaireId(), request.getBanqueId(), refinancement)));
    }

    @Operation(summary = "Modifier un refinancement existant")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<RefinancementResponse> update(
            @PathVariable Long id,
            @RequestBody Refinancement refinancement) {
        return ResponseEntity.ok(RefinancementMapper.toDto(refinancementService.update(id, refinancement)));
    }

    @Operation(summary = "Changer le statut d'un refinancement",
               description = "Valeurs possibles : EN_ATTENTE, APPROUVE, REJETE, CLOS")
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<RefinancementResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutOperation statut) {
        return ResponseEntity.ok(RefinancementMapper.toDto(refinancementService.changerStatut(id, statut)));
    }

    @Operation(summary = "Supprimer un refinancement")
    @ApiResponse(responseCode = "204", description = "Refinancement supprimé")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        refinancementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
