package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.PartenaireMapper;
import com.example.gestion_financement.dto.response.PartenaireResponse;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.service.PartenaireService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Partenaires", description = "Gestion des partenaires (clients et fournisseurs)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/partenaires")
@RequiredArgsConstructor
public class PartenaireController {

    private final PartenaireService partenaireService;

    @Operation(summary = "Liste paginée des partenaires",
               description = "Filtres optionnels : type (CLIENT, FOURNISSEUR) ou recherche par nom")
    @GetMapping
    public ResponseEntity<Page<PartenaireResponse>> findAll(
            @Parameter(description = "Filtrer par type") @RequestParam(required = false) TypePartenaire type,
            @Parameter(description = "Recherche par nom (insensible à la casse)") @RequestParam(required = false) String nom,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Partenaire> page;
        if (type != null)      page = partenaireService.findByType(type, pageable);
        else if (nom != null)  page = partenaireService.search(nom, pageable);
        else                   page = partenaireService.findAll(pageable);
        return ResponseEntity.ok(page.map(PartenaireMapper::toDto));
    }

    @Operation(summary = "Détail d'un partenaire par ID")
    @ApiResponse(responseCode = "404", description = "Partenaire introuvable")
    @GetMapping("/{id}")
    public ResponseEntity<PartenaireResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(PartenaireMapper.toDto(partenaireService.findById(id)));
    }

    @Operation(summary = "Liste paginée des clients uniquement")
    @GetMapping("/clients")
    public ResponseEntity<Page<PartenaireResponse>> findClients(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(partenaireService.findClients(pageable).map(PartenaireMapper::toDto));
    }

    @Operation(summary = "Liste paginée des fournisseurs uniquement")
    @GetMapping("/fournisseurs")
    public ResponseEntity<Page<PartenaireResponse>> findFournisseurs(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(partenaireService.findFournisseurs(pageable).map(PartenaireMapper::toDto));
    }

    @Operation(summary = "Créer un nouveau partenaire")
    @ApiResponse(responseCode = "201", description = "Partenaire créé")
    @PostMapping
    public ResponseEntity<PartenaireResponse> create(@Valid @RequestBody Partenaire partenaire) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PartenaireMapper.toDto(partenaireService.save(partenaire)));
    }

    @Operation(summary = "Modifier un partenaire existant")
    @PutMapping("/{id}")
    public ResponseEntity<PartenaireResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody Partenaire partenaire) {
        return ResponseEntity.ok(PartenaireMapper.toDto(partenaireService.update(id, partenaire)));
    }

    @Operation(summary = "Supprimer un partenaire")
    @ApiResponse(responseCode = "204", description = "Partenaire supprimé")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partenaireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
