package com.example.gestion_financement.metier;

import com.example.gestion_financement.dto.request.EscompteRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de validation des contraintes Bean Validation sur EscompteRequest.
 * Vérifie que @NotNull, @Positive, @Min, @Max, @FutureOrPresent
 * détectent correctement les données invalides.
 *
 * Aucun contexte Spring nécessaire — le Validator est instancié directement.
 */
@DisplayName("Validation des contraintes — EscompteRequest")
class EscompteRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ─── Requête valide ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Requête valide → aucune violation de contrainte")
    void requeteValide_aucuneViolation() {
        EscompteRequest request = new EscompteRequest(
                1L,
                1L,
                new BigDecimal("100000.00"),
                new BigDecimal("5.00"),
                30,
                LocalDate.now().plusDays(30)
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // ─── Montant invalide ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Montant = 0 → violation @Positive")
    void montantZero_violationPositive() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                BigDecimal.ZERO,           // ← invalide
                new BigDecimal("5.00"),
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("montant"));
    }

    @Test
    @DisplayName("Montant négatif → violation @Positive")
    void montantNegatif_violationPositive() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("-500.00"), // ← invalide
                new BigDecimal("5.00"),
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("montant") &&
                v.getMessage().contains("positif"));
    }

    @Test
    @DisplayName("Montant null → violation @NotNull")
    void montantNull_violationNotNull() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                null,                      // ← invalide
                new BigDecimal("5.00"),
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("montant"));
    }

    // ─── Taux invalide ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Taux = 0 → violation @Positive")
    void tauxZero_violationPositive() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("100000.00"),
                BigDecimal.ZERO,           // ← invalide
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("taux"));
    }

    @Test
    @DisplayName("Taux > 100 % → violation @DecimalMax")
    void tauxSuperieur100_violationDecimalMax() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("100000.00"),
                new BigDecimal("150.00"),  // ← invalide : > 100 %
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("taux") &&
                v.getMessage().contains("100"));
    }

    // ─── Durée invalide ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Durée = 0 → violation @Min(1)")
    void dureeZero_violationMin() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("100000.00"),
                new BigDecimal("5.00"),
                0,   // ← invalide
                null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("duree"));
    }

    @Test
    @DisplayName("Durée > 365 jours → violation @Max(365)")
    void dureeSuperieure365_violationMax() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("100000.00"),
                new BigDecimal("5.00"),
                400, // ← invalide : > 365 jours
                null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("duree") &&
                v.getMessage().contains("365"));
    }

    // ─── IDs invalides ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("partenaireId null → violation @NotNull")
    void partenaireIdNull_violationNotNull() {
        EscompteRequest request = new EscompteRequest(
                null,                       // ← invalide
                1L,
                new BigDecimal("100000.00"),
                new BigDecimal("5.00"),
                30, null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("partenaireId"));
    }

    // ─── Date d'échéance invalide ─────────────────────────────────────────────────

    @Test
    @DisplayName("Date échéance dans le passé → violation @FutureOrPresent")
    void dateEcheancePasse_violationFutureOrPresent() {
        EscompteRequest request = new EscompteRequest(
                1L, 1L,
                new BigDecimal("100000.00"),
                new BigDecimal("5.00"),
                30,
                LocalDate.now().minusDays(1) // ← invalide : dans le passé
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("dateEcheance"));
    }

    // ─── Violations multiples ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Plusieurs champs invalides → plusieurs violations retournées")
    void plusieursChamps_plusieursViolations() {
        // Tout est invalide sauf banqueId
        EscompteRequest request = new EscompteRequest(
                null,             // partenaireId manquant
                1L,
                BigDecimal.ZERO,  // montant invalide
                BigDecimal.ZERO,  // taux invalide
                0,                // durée invalide
                null
        );

        Set<ConstraintViolation<EscompteRequest>> violations = validator.validate(request);

        // On attend au moins 4 violations
        assertThat(violations.size()).isGreaterThanOrEqualTo(4);
    }
}
