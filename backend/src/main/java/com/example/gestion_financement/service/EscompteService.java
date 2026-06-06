package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.entity.Escompte;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.exception.FinancementRefuseException;
import com.example.gestion_financement.exception.MontantInvalideException;
import com.example.gestion_financement.exception.PartenaireIntrouvableException;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.EscompteRepository;
import com.example.gestion_financement.repository.PartenaireRepository;
import com.example.gestion_financement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Transactional
public class EscompteService {

    // Seuil au-delà duquel la banque refuse automatiquement le financement
    private static final BigDecimal SEUIL_REFUS_BANQUE = new BigDecimal("5000000");

    private final EscompteRepository    escompteRepository;
    private final PartenaireRepository  partenaireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BanqueService         banqueService;
    private final LogActionService      logActionService;

    // ─── Lecture paginée ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Escompte> findAll(Pageable pageable) {
        return escompteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Escompte> findByStatut(StatutOperation statut, Pageable pageable) {
        return escompteRepository.findByStatut(statut, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Escompte> findByPartenaire(Long partenaireId, Pageable pageable) {
        return escompteRepository.findByPartenaireId(partenaireId, pageable);
    }

    // ─── Lecture unitaire ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Escompte findById(Long id) {
        return escompteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escompte non trouvé avec l'id : " + id));
    }

    // ─── Écriture ─────────────────────────────────────────────────────────────────

    /**
     * Crée un escompte après validation métier :
     *  1. Le montant doit être > 0
     *  2. Le partenaire doit exister et être de type CLIENT
     *  3. La banque ne doit pas refuser le financement
     */
    public Escompte create(Long partenaireId, Long banqueId, Escompte escompte, String userEmail) {

        // Règle 1 : montant valide
        validerMontant(escompte.getMontant());

        // Règle 2 : partenaire existe et est de type CLIENT
        Partenaire partenaire = trouverPartenaire(partenaireId);
        if (partenaire.getType() != TypePartenaire.CLIENT) {
            throw new IllegalArgumentException(
                    "L'escompte est réservé aux partenaires de type CLIENT.");
        }

        // Règle 3 : banque accepte le financement
        Banque banque = banqueService.findById(banqueId);
        verifierAcceptationBanque(escompte.getMontant(), banque);

        escompte.setPartenaire(partenaire);
        escompte.setBanque(banque);

        // Le taux est toujours imposé par la banque
        if (banque.getTauxEscompte() == null) {
            throw new IllegalArgumentException(
                "La banque « " + banque.getNom() + " » n'a pas de taux d'escompte configuré. Veuillez le renseigner dans la fiche banque.");
        }
        escompte.setTaux(banque.getTauxEscompte());

        // Traçabilité : enregistre l'utilisateur qui a créé l'escompte
        if (userEmail != null) {
            utilisateurRepository.findByEmail(userEmail).ifPresent(escompte::setCreePar);
        }

        escompte.calculerAgios();
        Escompte saved = escompteRepository.save(escompte);

        // Référence courte : ESC-{année}-{id sur 4 chiffres min}
        saved.setReference(String.format("ESC-%d-%04d",
            java.time.LocalDate.now().getYear(), saved.getId()));
        saved = escompteRepository.save(saved);

        logActionService.logParEmail(userEmail, "CRÉATION", "Escompte", saved.getId(),
            "Réf : " + saved.getReference() + " — Montant : " + saved.getMontant() + " MAD"
            + " — Client : " + partenaire.getNom() + " — Banque : " + banque.getNom());
        return saved;
    }

    /**
     * Soumet (ou re-soumet) un escompte pour validation par le responsable.
     * Accessible à l'agent financier. Interdit si l'escompte est déjà approuvé.
     */
    public Escompte soumettre(Long id, String userEmail) {
        Escompte escompte = findById(id);
        if (escompte.getStatut() == StatutOperation.APPROUVE)
            throw new IllegalArgumentException("Un escompte déjà approuvé ne peut pas être resoumis.");
        if (escompte.getStatut() == StatutOperation.ANNULE)
            throw new IllegalArgumentException("Un escompte annulé ne peut pas être resoumis.");
        escompte.setStatut(StatutOperation.EN_ATTENTE);
        Escompte saved = escompteRepository.save(escompte);
        logActionService.logParEmail(userEmail, "SOUMISSION", "Escompte", id,
            "Réf : " + escompte.getReference() + " soumis pour validation");
        return saved;
    }

    public Escompte update(Long id, Escompte updated) {
        validerMontant(updated.getMontant());

        Escompte existing = findById(id);
        existing.setMontant(updated.getMontant());
        existing.setDuree(updated.getDuree());
        existing.setDateEcheance(updated.getDateEcheance());
        // Le taux reste celui de la banque (défini à la création)
        existing.calculerAgios();
        return escompteRepository.save(existing);
    }

    public Escompte changerStatut(Long id, StatutOperation statut, String userEmail) {
        Escompte escompte = findById(id);
        if (statut == StatutOperation.APPROUVE)
            verifierAcceptationBanque(escompte.getMontant(), escompte.getBanque());
        StatutOperation ancien = escompte.getStatut();
        escompte.setStatut(statut);
        Escompte saved = escompteRepository.save(escompte);
        String action = switch (statut) {
            case APPROUVE  -> "APPROBATION";
            case REJETE    -> "REJET";
            case ANNULE    -> "ANNULATION";
            default        -> "MODIFICATION";
        };
        logActionService.logParEmail(userEmail, action, "Escompte", id,
            "Réf : " + escompte.getReference() + " — Statut : " + ancien + " → " + statut);
        return saved;
    }

    public void delete(Long id, String userEmail) {
        Escompte escompte = findById(id);
        logActionService.logParEmail(userEmail, "SUPPRESSION", "Escompte", id,
            "Réf : " + escompte.getReference() + " supprimé");
        escompteRepository.deleteById(id);
    }

    // ─── Agrégats ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BigDecimal getTotalMontantApprouve() {
        return escompteRepository.sumMontantApprouve();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAgiosApprouve() {
        return escompteRepository.sumAgiosApprouve();
    }

    // ─── Méthodes de validation privées ──────────────────────────────────────────

    /**
     * Vérifie que le montant est strictement positif.
     * Lève MontantInvalideException sinon.
     */
    private void validerMontant(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontantInvalideException();
        }
    }

    /**
     * Recherche un partenaire par id.
     * Lève PartenaireIntrouvableException si absent (au lieu de ResourceNotFoundException).
     */
    private Partenaire trouverPartenaire(Long partenaireId) {
        return partenaireRepository.findById(partenaireId)
                .orElseThrow(() -> new PartenaireIntrouvableException(partenaireId));
    }

    /**
     * Simule la décision de la banque : refuse si le montant dépasse le seuil.
     * En production, cela pourrait être un appel à un service externe.
     */
    private void verifierAcceptationBanque(BigDecimal montant, Banque banque) {
        if (montant != null && montant.compareTo(SEUIL_REFUS_BANQUE) > 0) {
            throw new FinancementRefuseException(
                    "montant " + montant + " MAD dépasse le plafond autorisé de "
                    + SEUIL_REFUS_BANQUE + " MAD");
        }
    }
}
