package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.UtilisateurMapper;
import com.example.gestion_financement.dto.response.UtilisateurResponse;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.RoleUtilisateur;
import com.example.gestion_financement.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Tag(name = "Utilisateurs", description = "Gestion des comptes utilisateurs — ADMIN uniquement")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @Operation(summary = "Liste paginée des utilisateurs",
               description = "Filtre optionnel par rôle : ADMIN, RESPONSABLE, AGENT_FINANCIER")
    @GetMapping
    public ResponseEntity<Page<UtilisateurResponse>> findAll(
            @Parameter(description = "Filtrer par rôle") @RequestParam(required = false) RoleUtilisateur role,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Utilisateur> page = (role != null)
                ? utilisateurService.findByRole(role, pageable)
                : utilisateurService.findAll(pageable);
        return ResponseEntity.ok(page.map(UtilisateurMapper::toDto));
    }

    @Operation(summary = "Détail d'un utilisateur par ID")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UtilisateurMapper.toDto(utilisateurService.findById(id)));
    }

    @Operation(summary = "Créer un compte utilisateur")
    @ApiResponse(responseCode = "201", description = "Utilisateur créé")
    @PostMapping
    public ResponseEntity<UtilisateurResponse> create(@Valid @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UtilisateurMapper.toDto(utilisateurService.save(utilisateur)));
    }

    @Operation(summary = "Modifier un utilisateur existant")
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(UtilisateurMapper.toDto(utilisateurService.update(id, utilisateur)));
    }

    @Operation(summary = "Activer / désactiver un compte utilisateur")
    @PatchMapping("/{id}/toggle-actif")
    public ResponseEntity<UtilisateurResponse> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(UtilisateurMapper.toDto(utilisateurService.toggleActif(id)));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
