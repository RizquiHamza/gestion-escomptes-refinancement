package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.entity.Escompte;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.exception.FinancementRefuseException;
import com.example.gestion_financement.exception.MontantInvalideException;
import com.example.gestion_financement.exception.PartenaireIntrouvableException;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.EscompteRepository;
import com.example.gestion_financement.repository.PartenaireRepository;
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

    private final EscompteRepository   escompteRepository;
    private final PartenaireRepository  partenaireRepository;
    private final BanqueService         banqueService;

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
    public Escompte create(Long partenaireId, Long banqueId, Escompte escompte) {

        // Règle 1 : montant valide
        validerMontant(escompte.getMontant());

        // Règle 2 : partenaire existe
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
        escompte.calculerAgios();
        return escompteRepository.save(escompte);
    }

    public Escompte update(Long id, Escompte updated) {
        // Règle : montant valide
        validerMontant(updated.getMontant());

        Escompte existing = findById(id);
        existing.setMontant(updated.getMontant());
        existing.setTaux(updated.getTaux());
        existing.setDuree(updated.getDuree());
        existing.setDateEcheance(updated.getDateEcheance());
        existing.calculerAgios();
        return escompteRepository.save(existing);
    }

    public Escompte changerStatut(Long id, StatutOperation statut) {
        Escompte escompte = findById(id);

        // Règle : si on tente d'approuver, re-vérifier la banque
        if (statut == StatutOperation.APPROUVE) {
            verifierAcceptationBanque(escompte.getMontant(), escompte.getBanque());
        }

        escompte.setStatut(statut);
        return escompteRepository.save(escompte);
    }

    public void delete(Long id) {
        findById(id);
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
