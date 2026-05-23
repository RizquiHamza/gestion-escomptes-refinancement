package com.example.gestion_financement.exception;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.entity.Escompte;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.repository.EscompteRepository;
import com.example.gestion_financement.repository.PartenaireRepository;
import com.example.gestion_financement.service.BanqueService;
import com.example.gestion_financement.service.EscompteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires des exceptions métier dans EscompteService.
 * Utilise Mockito pour simuler les dépendances (sans base de données).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests des exceptions métier — EscompteService")
class EscompteServiceExceptionTest {

    @Mock private EscompteRepository   escompteRepository;
    @Mock private PartenaireRepository partenaireRepository;
    @Mock private BanqueService        banqueService;

    @InjectMocks
    private EscompteService escompteService;

    private Banque banqueValide;
    private Partenaire partenaireClient;

    @BeforeEach
    void setUp() {
        banqueValide = new Banque();
        banqueValide.setId(1L);
        banqueValide.setNom("Banque Test");
        banqueValide.setCode("BT");

        partenaireClient = new Partenaire();
        partenaireClient.setId(1L);
        partenaireClient.setNom("Client Test");
        partenaireClient.setType(TypePartenaire.CLIENT);
    }

    // ─── MontantInvalideException ─────────────────────────────────────────────────

    @Test
    @DisplayName("create() → MontantInvalideException si montant = 0")
    void create_montantZero_leveMontantInvalideException() {
        Escompte escompte = Escompte.builder()
                .montant(BigDecimal.ZERO)
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                .isInstanceOf(MontantInvalideException.class)
                .hasMessageContaining("Le montant doit être supérieur à 0");
    }

    @Test
    @DisplayName("create() → MontantInvalideException si montant négatif")
    void create_montantNegatif_leveMontantInvalideException() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("-100"))
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                .isInstanceOf(MontantInvalideException.class);
    }

    // ─── PartenaireIntrouvableException ──────────────────────────────────────────

    @Test
    @DisplayName("create() → PartenaireIntrouvableException si partenaire absent")
    void create_partenaireInexistant_levePartenaireIntrouvableException() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("50000"))
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        // Le partenaire 99L n'existe pas
        when(partenaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> escompteService.create(99L, 1L, escompte))
                .isInstanceOf(PartenaireIntrouvableException.class)
                .hasMessageContaining("Partenaire introuvable");
    }

    // ─── FinancementRefuseException ───────────────────────────────────────────────

    @Test
    @DisplayName("create() → FinancementRefuseException si montant dépasse le plafond")
    void create_montantDepasePlafond_leveFinancementRefuseException() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("6000000")) // > 5 000 000 MAD
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
        when(banqueService.findById(1L)).thenReturn(banqueValide);

        assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                .isInstanceOf(FinancementRefuseException.class)
                .hasMessageContaining("refusée par la banque");
    }

    @Test
    @DisplayName("create() → succès si toutes les règles sont respectées")
    void create_donneesValides_sauvegardeEscompte() {
        Escompte escompte = Escompte.builder()
                .montant(new BigDecimal("100000"))
                .taux(new BigDecimal("5.00"))
                .duree(30)
                .build();

        when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
        when(banqueService.findById(1L)).thenReturn(banqueValide);
        when(escompteRepository.save(escompte)).thenReturn(escompte);

        Escompte result = escompteService.create(1L, 1L, escompte);

        // Vérifie que le partenaire et la banque sont bien associés
        org.assertj.core.api.Assertions.assertThat(result.getPartenaire()).isEqualTo(partenaireClient);
        org.assertj.core.api.Assertions.assertThat(result.getBanque()).isEqualTo(banqueValide);
    }

    // ─── Test du code d'erreur dans l'exception ───────────────────────────────────

    @Test
    @DisplayName("Chaque exception porte le bon code métier")
    void exceptions_portentLeBonCode() {
        assertThatThrownBy(() -> { throw new MontantInvalideException(); })
                .isInstanceOf(MontantInvalideException.class)
                .extracting(e -> ((MontantInvalideException) e).getCode())
                .isEqualTo("MONTANT_INVALIDE");

        assertThatThrownBy(() -> { throw new PartenaireIntrouvableException(1L); })
                .isInstanceOf(PartenaireIntrouvableException.class)
                .extracting(e -> ((PartenaireIntrouvableException) e).getCode())
                .isEqualTo("PARTENAIRE_INTROUVABLE");

        assertThatThrownBy(() -> { throw new FinancementRefuseException(); })
                .isInstanceOf(FinancementRefuseException.class)
                .extracting(e -> ((FinancementRefuseException) e).getCode())
                .isEqualTo("FINANCEMENT_REFUSE");
    }
}
