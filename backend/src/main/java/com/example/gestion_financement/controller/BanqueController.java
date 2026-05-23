package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.mapper.BanqueMapper;
import com.example.gestion_financement.dto.response.BanqueResponse;
import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.service.BanqueService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Banques", description = "Gestion des banques partenaires")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/banques")
@RequiredArgsConstructor
public class BanqueController {

    private final BanqueService banqueService;

    @Operation(summary = "Liste paginée des banques")
    @GetMapping
    public ResponseEntity<Page<BanqueResponse>> findAll(
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(banqueService.findAll(pageable).map(BanqueMapper::toDto));
    }

    @Operation(summary = "Détail d'une banque par ID")
    @ApiResponse(responseCode = "404", description = "Banque introuvable")
    @GetMapping("/{id}")
    public ResponseEntity<BanqueResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BanqueMapper.toDto(banqueService.findById(id)));
    }

    @Operation(summary = "Créer une nouvelle banque")
    @ApiResponse(responseCode = "201", description = "Banque créée")
    @PostMapping
    public ResponseEntity<BanqueResponse> create(@Valid @RequestBody Banque banque) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BanqueMapper.toDto(banqueService.save(banque)));
    }

    @Operation(summary = "Modifier une banque existante")
    @PutMapping("/{id}")
    public ResponseEntity<BanqueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody Banque banque) {
        return ResponseEntity.ok(BanqueMapper.toDto(banqueService.update(id, banque)));
    }

    @Operation(summary = "Supprimer une banque")
    @ApiResponse(responseCode = "204", description = "Banque supprimée")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        banqueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
