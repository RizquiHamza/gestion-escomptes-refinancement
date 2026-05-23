package com.example.gestion_financement.metier;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.entity.Escompte;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.exception.FinancementRefuseException;
import com.example.gestion_financement.exception.MontantInvalideException;
import com.example.gestion_financement.exception.PartenaireIntrouvableException;
import com.example.gestion_financement.repository.EscompteRepository;
import com.example.gestion_financement.repository.PartenaireRepository;
import com.example.gestion_financement.service.BanqueService;
import com.example.gestion_financement.service.EscompteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires des règles métier dans EscompteService.
 * Utilise Mockito pour simuler Repository et BanqueService.
 * Organisé par groupes (@Nested) pour plus de clarté dans les rapports de test.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Règles métier — EscompteService")
class EscompteServiceMetierTest {

    @Mock private EscompteRepository   escompteRepository;
    @Mock private PartenaireRepository partenaireRepository;
    @Mock private BanqueService        banqueService;

    @InjectMocks
    private EscompteService escompteService;

    private Banque     banqueTest;
    private Partenaire partenaireClient;
    private Partenaire partenaireFournisseur;

    @BeforeEach
    void setUp() {
        banqueTest = new Banque();
        banqueTest.setId(1L);
        banqueTest.setNom("Banque Al Maghrib");
        banqueTest.setCode("BAM");

        partenaireClient = new Partenaire();
        partenaireClient.setId(1L);
        partenaireClient.setNom("Société ABC");
        partenaireClient.setType(TypePartenaire.CLIENT);

        partenaireFournisseur = new Partenaire();
        partenaireFournisseur.setId(2L);
        partenaireFournisseur.setNom("Fournisseur XYZ");
        partenaireFournisseur.setType(TypePartenaire.FOURNISSEUR);
    }

    // ─── Groupe 1 : Validation du montant ─────────────────────────────────────────

    @Nested
    @DisplayName("Validation du montant")
    class MontantValidation {

        @Test
        @DisplayName("Montant = 0 → MontantInvalideException")
        void montantZero_leveMontantInvalideException() {
            Escompte escompte = creerEscompte("0", "5.00", 30);

            assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                    .isInstanceOf(MontantInvalideException.class)
                    .hasMessageContaining("supérieur à 0");
        }

        @Test
        @DisplayName("Montant négatif → MontantInvalideException")
        void montantNegatif_leveMontantInvalideException() {
            Escompte escompte = creerEscompte("-1000", "5.00", 30);

            assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                    .isInstanceOf(MontantInvalideException.class);
        }

        @Test
        @DisplayName("MontantInvalideException porte le code MONTANT_INVALIDE")
        void montantInvalide_codeErreurCorrect() {
            Escompte escompte = creerEscompte("0", "5.00", 30);

            assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                    .isInstanceOf(MontantInvalideException.class)
                    .extracting(e -> ((MontantInvalideException) e).getCode())
                    .isEqualTo("MONTANT_INVALIDE");
        }
    }

    // ─── Groupe 2 : Partenaire introuvable ────────────────────────────────────────

    @Nested
    @DisplayName("Partenaire introuvable")
    class PartenaireInconnu {

        @Test
        @DisplayName("Partenaire inexistant → PartenaireIntrouvableException")
        void partenaireAbsent_levePartenaireIntrouvableException() {
            Escompte escompte = creerEscompte("50000", "5.00", 30);
            when(partenaireRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> escompteService.create(99L, 1L, escompte))
                    .isInstanceOf(PartenaireIntrouvableException.class)
                    .hasMessageContaining("Partenaire introuvable");
        }

        @Test
        @DisplayName("PartenaireIntrouvableException contient l'ID demandé")
        void partenaireAbsent_messageContientId() {
            Escompte escompte = creerEscompte("50000", "5.00", 30);
            when(partenaireRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> escompteService.create(42L, 1L, escompte))
                    .isInstanceOf(PartenaireIntrouvableException.class)
                    .hasMessageContaining("42");
        }

        @Test
        @DisplayName("Partenaire FOURNISSEUR → IllegalArgumentException (seuls les CLIENTS sont acceptés)")
        void partenaireFournisseur_leveIllegalArgument() {
            Escompte escompte = creerEscompte("50000", "5.00", 30);
            when(partenaireRepository.findById(2L)).thenReturn(Optional.of(partenaireFournisseur));

            assertThatThrownBy(() -> escompteService.create(2L, 1L, escompte))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CLIENT");
        }
    }

    // ─── Groupe 3 : Refus de financement bancaire ─────────────────────────────────

    @Nested
    @DisplayName("Refus de financement bancaire")
    class FinancementRefus {

        @Test
        @DisplayName("Montant > plafond (5 000 000 MAD) → FinancementRefuseException")
        void montantDepasePlafond_leveFinancementRefuseException() {
            Escompte escompte = creerEscompte("6000000", "5.00", 30);
            when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
            when(banqueService.findById(1L)).thenReturn(banqueTest);

            assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                    .isInstanceOf(FinancementRefuseException.class)
                    .hasMessageContaining("refusée par la banque");
        }

        @Test
        @DisplayName("FinancementRefuseException porte le code FINANCEMENT_REFUSE")
        void refusBanque_codeErreurCorrect() {
            Escompte escompte = creerEscompte("6000000", "5.00", 30);
            when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
            when(banqueService.findById(1L)).thenReturn(banqueTest);

            assertThatThrownBy(() -> escompteService.create(1L, 1L, escompte))
                    .isInstanceOf(FinancementRefuseException.class)
                    .extracting(e -> ((FinancementRefuseException) e).getCode())
                    .isEqualTo("FINANCEMENT_REFUSE");
        }

        @Test
        @DisplayName("Montant exactement = plafond → accepté (limite incluse)")
        void montantExactementPlafond_accepte() {
            Escompte escompte = creerEscompte("5000000", "5.00", 30);
            when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
            when(banqueService.findById(1L)).thenReturn(banqueTest);
            when(escompteRepository.save(any())).thenReturn(escompte);

            // Ne doit pas lever d'exception
            Escompte result = escompteService.create(1L, 1L, escompte);
            assertThat(result).isNotNull();
        }
    }

    // ─── Groupe 4 : Création réussie ──────────────────────────────────────────────

    @Nested
    @DisplayName("Création réussie")
    class CreationReussie {

        @Test
        @DisplayName("Données valides → escompte créé avec partenaire et banque associés")
        void creerEscompte_donneesValides_succees() {
            Escompte escompte = creerEscompte("100000", "5.00", 30);
            when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
            when(banqueService.findById(1L)).thenReturn(banqueTest);
            when(escompteRepository.save(any())).thenReturn(escompte);

            Escompte result = escompteService.create(1L, 1L, escompte);

            assertThat(result.getPartenaire()).isEqualTo(partenaireClient);
            assertThat(result.getBanque()).isEqualTo(banqueTest);
            verify(escompteRepository, times(1)).save(escompte);
        }

        @Test
        @DisplayName("Données valides → agios calculés automatiquement")
        void creerEscompte_agiosCalculesAutomatiquement() {
            Escompte escompte = creerEscompte("100000", "5.00", 30);
            when(partenaireRepository.findById(1L)).thenReturn(Optional.of(partenaireClient));
            when(banqueService.findById(1L)).thenReturn(banqueTest);
            when(escompteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Escompte result = escompteService.create(1L, 1L, escompte);

            // agios = 100000 × 5 × 30 / 36000 = 416,67
            assertThat(result.getAgios())
                    .isEqualByComparingTo(new BigDecimal("416.67"));
            assertThat(result.getNetRecu())
                    .isEqualByComparingTo(new BigDecimal("99583.33"));
        }

        @Test
        @DisplayName("Statut initial → EN_ATTENTE")
        void creerEscompte_statutInitialEnAttente() {
            Escompte escompte = Escompte.builder()
                    .montant(new BigDecimal("100000"))
                    .taux(new BigDecimal("5.00"))
                    .duree(30)
                    .build();

            // Statut par défaut défini dans l'entité via @Builder.Default
            assertThat(escompte.getStatut()).isEqualTo(StatutOperation.EN_ATTENTE);
        }
    }

    // ─── Utilitaire de construction d'escomptes ───────────────────────────────────

    private Escompte creerEscompte(String montant, String taux, int duree) {
        return Escompte.builder()
                .montant(new BigDecimal(montant))
                .taux(new BigDecimal(taux))
                .duree(duree)
                .build();
    }
}
