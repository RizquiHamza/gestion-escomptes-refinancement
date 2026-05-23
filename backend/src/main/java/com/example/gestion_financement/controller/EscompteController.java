package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.EscompteMapper;
import com.example.gestion_financement.dto.request.EscompteRequest;
import com.example.gestion_financement.dto.response.EscompteResponse;
import com.example.gestion_financement.entity.Escompte;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.service.EscompteService;
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

@Tag(name = "Escomptes", description = "Gestion des opérations d'escompte (clients)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/escomptes")
@RequiredArgsConstructor
public class EscompteController {

    private final EscompteService escompteService;

    @Operation(summary = "Liste paginée des escomptes",
               description = "Filtres optionnels : statut (EN_ATTENTE, APPROUVE, REJETE, CLOS) ou partenaireId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page d'escomptes retournée"),
        @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<Page<EscompteResponse>> findAll(
            @Parameter(description = "Filtrer par statut") @RequestParam(required = false) StatutOperation statut,
            @Parameter(description = "Filtrer par ID partenaire") @RequestParam(required = false) Long partenaireId,
            @PageableDefault(size = 10, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Escompte> page;
        if (statut != null)           page = escompteService.findByStatut(statut, pageable);
        else if (partenaireId != null) page = escompteService.findByPartenaire(partenaireId, pageable);
        else                           page = escompteService.findAll(pageable);
        return ResponseEntity.ok(page.map(EscompteMapper::toDto));
    }

    @Operation(summary = "Détail d'un escompte par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Escompte trouvé"),
        @ApiResponse(responseCode = "404", description = "Escompte introuvable")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<EscompteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(EscompteMapper.toDto(escompteService.findById(id)));
    }

    @Operation(summary = "Créer un nouvel escompte")
    @ApiResponse(responseCode = "201", description = "Escompte créé")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT_FINANCIER')")
    public ResponseEntity<EscompteResponse> create(@Valid @RequestBody EscompteRequest request) {
        Escompte escompte = Escompte.builder()
                .montant(request.getMontant())
                .taux(request.getTaux())
                .duree(request.getDuree())
                .dateEcheance(request.getDateEcheance())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EscompteMapper.toDto(
                        escompteService.create(request.getPartenaireId(), request.getBanqueId(), escompte)));
    }

    @Operation(summary = "Modifier un escompte existant")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<EscompteResponse> update(
            @PathVariable Long id,
            @RequestBody Escompte escompte) {
        return ResponseEntity.ok(EscompteMapper.toDto(escompteService.update(id, escompte)));
    }

    @Operation(summary = "Changer le statut d'un escompte",
               description = "Valeurs possibles : EN_ATTENTE, APPROUVE, REJETE, CLOS")
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<EscompteResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutOperation statut) {
        return ResponseEntity.ok(EscompteMapper.toDto(escompteService.changerStatut(id, statut)));
    }

    @Operation(summary = "Supprimer un escompte")
    @ApiResponse(responseCode = "204", description = "Escompte supprimé")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        escompteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
