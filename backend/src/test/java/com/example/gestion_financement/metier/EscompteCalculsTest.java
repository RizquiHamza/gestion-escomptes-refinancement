package com.example.gestion_financement.metier;

import com.example.gestion_financement.entity.Escompte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires purs pour les méthodes de calcul financier de l'entité Escompte.
 * Aucune base de données, aucun mock — uniquement la logique métier.
 *
 * Formule appliquée :
 *   agios    = montant × taux(%) × durée(jours) / 36 000
 *   net reçu = montant − agios
 */
@DisplayName("Calculs financiers — Escompte")
class EscompteCalculsTest {

    // ─── Cas de base ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("calculerAgios() — cas standard : 100 000 MAD, 5 %, 30 jours")
    void calculerAgios_casStandard() {
        /*
         * agios = 100 000 × 5 × 30 / 36 000
         *       = 15 000 000 / 36 000
         *       = 416,67 MAD
         */
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("100000.00"))
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getAgios())
                .isEqualByComparingTo(new BigDecimal("416.67"));
    }

    @Test
    @DisplayName("calculerMontantNet() — net reçu = montant − agios")
    void calculerMontantNet_casStandard() {
        /*
         * agios    = 416,67 MAD
         * net reçu = 100 000 − 416,67 = 99 583,33 MAD
         */
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("100000.00"))
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getNetRecu())
                .isEqualByComparingTo(new BigDecimal("99583.33"));
    }

    // ─── Tests paramétrés (plusieurs jeux de données) ────────────────────────────

    /**
     * Vérifie la formule sur plusieurs cas réels.
     * Format CSV : montant, taux, duree, agiosAttendu, netRecuAttendu
     */
    @ParameterizedTest(name = "{0} MAD × {1}% × {2}j → agios={3}, net={4}")
    @CsvSource({
        // montant,   taux, duree, agiosAttendu, netRecuAttendu
        "100000.00,   5.00,   30,     416.67,    99583.33",
        "500000.00,   7.50,   90,    9375.00,   490625.00",
        "250000.00,   4.00,   45,    1250.00,   248750.00",
        "1000000.00,  3.00,  180,   15000.00,   985000.00",
        "75000.00,    6.00,   60,     750.00,    74250.00"
    })
    @DisplayName("calculerAgios() — table de vérité financière")
    void calculerAgios_tableDeVerite(
            String montant, String taux, int duree,
            String agiosAttendu, String netRecuAttendu) {

        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal(montant))
                .taux(new BigDecimal(taux))
                .duree(duree)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getAgios())
                .as("Agios pour %s MAD à %s%% sur %d jours", montant, taux, duree)
                .isEqualByComparingTo(new BigDecimal(agiosAttendu));

        assertThat(escompte.getNetRecu())
                .as("Net reçu pour %s MAD à %s%% sur %d jours", montant, taux, duree)
                .isEqualByComparingTo(new BigDecimal(netRecuAttendu));
    }

    // ─── Cas limites ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("calculerAgios() — aucun calcul si montant est null")
    void calculerAgios_montantNull_pasDeCalcul() {
        Escompte escompte = Escompte.builder()
                .montant(null)
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        // Ne doit pas lancer d'exception
        escompte.calculerAgios();

        assertThat(escompte.getAgios()).isNull();
        assertThat(escompte.getNetRecu()).isNull();
    }

    @Test
    @DisplayName("calculerAgios() — aucun calcul si taux est null")
    void calculerAgios_tauxNull_pasDeCalcul() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("100000.00"))
                .taux(null)
                .duree(30)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getAgios()).isNull();
    }

    @Test
    @DisplayName("calculerAgios() — aucun calcul si durée est null")
    void calculerAgios_dureeNull_pasDeCalcul() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("100000.00"))
                .taux(new BigDecimal("5.00"))
                .duree(null)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getAgios()).isNull();
    }

    @Test
    @DisplayName("calculerAgios() — agios toujours inférieur au montant")
    void calculerAgios_agiosInferieurAuMontant() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("500000.00"))
                .taux(new BigDecimal("10.00"))
                .duree(180)
                .build();

        escompte.calculerAgios();

        assertThat(escompte.getAgios())
                .isLessThan(escompte.getMontant());
        assertThat(escompte.getNetRecu())
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("netRecu = montant − agios (invariant comptable)")
    void invariantComptable_netRecuEgalMontantMoinsAgios() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("300000.00"))
                .taux(new BigDecimal("6.50"))
                .duree(60)
                .build();

        escompte.calculerAgios();

        BigDecimal netAttendu = escompte.getMontant().subtract(escompte.getAgios());
        assertThat(escompte.getNetRecu())
                .isEqualByComparingTo(netAttendu);
    }
}
