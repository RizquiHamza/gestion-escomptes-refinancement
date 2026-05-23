package com.example.gestion_financement.exception;

import com.example.gestion_financement.controller.TestMetierController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du GlobalExceptionHandler via MockMvc (standaloneSetup).
 * Aucun contexte Spring Security requis — on câble directement le contrôleur
 * et le handler d'exceptions, ce qui rend ces tests légers et rapides.
 */
@DisplayName("Tests GlobalExceptionHandler — réponses JSON des erreurs métier")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestMetierController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /financement-refuse → 422 + code FINANCEMENT_REFUSE")
    void financementRefuse_retourne422() throws Exception {
        mockMvc.perform(get("/api/test/exceptions/financement-refuse"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("FINANCEMENT_REFUSE"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("refusée par la banque")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /partenaire-introuvable → 404 + code PARTENAIRE_INTROUVABLE")
    void partenaireIntrouvable_retourne404() throws Exception {
        mockMvc.perform(get("/api/test/exceptions/partenaire-introuvable"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PARTENAIRE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Partenaire introuvable")));
    }

    @Test
    @DisplayName("GET /montant-invalide → 400 + code MONTANT_INVALIDE")
    void montantInvalide_retourne400() throws Exception {
        mockMvc.perform(get("/api/test/exceptions/montant-invalide"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MONTANT_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("supérieur à 0")));
    }

    @Test
    @DisplayName("GET /valider-montant?montant=-50 → 400 + message personnalisé")
    void validerMontantNegatif_retourne400() throws Exception {
        mockMvc.perform(get("/api/test/exceptions/valider-montant").param("montant", "-50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONTANT_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("-50")));
    }

    @Test
    @DisplayName("GET /valider-montant?montant=1000 → 200 OK")
    void validerMontantPositif_retourne200() throws Exception {
        mockMvc.perform(get("/api/test/exceptions/valider-montant").param("montant", "1000"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("Montant valide")));
    }
}
