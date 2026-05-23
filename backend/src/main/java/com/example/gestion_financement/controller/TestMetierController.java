package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.response.ErrorResponse;
import com.example.gestion_financement.exception.FinancementRefuseException;
import com.example.gestion_financement.exception.MontantInvalideException;
import com.example.gestion_financement.exception.PartenaireIntrouvableException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Endpoints de test pour vérifier le comportement des exceptions métier.
 * À utiliser uniquement en développement / Swagger.
 */
@Tag(name = "Tests Métier", description = "Endpoints de test des exceptions métier — développement uniquement")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/test/exceptions")
public class TestMetierController {

    @Operation(
        summary = "Tester : banque refuse le financement",
        description = "Déclenche FinancementRefuseException → HTTP 422",
        responses = @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    )
    @GetMapping("/financement-refuse")
    public ResponseEntity<Void> testFinancementRefuse() {
        throw new FinancementRefuseException();
    }

    @Operation(
        summary = "Tester : partenaire introuvable",
        description = "Déclenche PartenaireIntrouvableException → HTTP 404",
        responses = @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    )
    @GetMapping("/partenaire-introuvable")
    public ResponseEntity<Void> testPartenaireIntrouvable(
            @RequestParam(defaultValue = "99") Long id) {
        throw new PartenaireIntrouvableException(id);
    }

    @Operation(
        summary = "Tester : montant invalide",
        description = "Déclenche MontantInvalideException → HTTP 400",
        responses = @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    )
    @GetMapping("/montant-invalide")
    public ResponseEntity<Void> testMontantInvalide() {
        throw new MontantInvalideException();
    }

    @Operation(
        summary = "Tester : montant invalide (valeur envoyée)",
        description = "Simule la validation d'un montant fourni par le client → HTTP 400 si ≤ 0"
    )
    @GetMapping("/valider-montant")
    public ResponseEntity<String> validerMontant(@RequestParam BigDecimal montant) {
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontantInvalideException("Valeur reçue : " + montant);
        }
        return ResponseEntity.ok("Montant valide : " + montant + " MAD");
    }
}
