package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.entity.Refinancement;
import com.example.gestion_financement.enums.StatutOperation;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.RefinancementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class RefinancementService {

    private final RefinancementRepository refinancementRepository;
    private final PartenaireService        partenaireService;
    private final BanqueService            banqueService;
    private final LogActionService         logActionService;

    // ─── Lecture paginée ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Refinancement> findAll(Pageable pageable) {
        return refinancementRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Refinancement> findByStatut(StatutOperation statut, Pageable pageable) {
        return refinancementRepository.findByStatut(statut, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Refinancement> findByPartenaire(Long partenaireId, Pageable pageable) {
        return refinancementRepository.findByPartenaireId(partenaireId, pageable);
    }

    // ─── Lecture unitaire ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Refinancement findById(Long id) {
        return refinancementRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Refinancement non trouvé avec l'id: " + id));
    }

    // ─── Écriture ─────────────────────────────────────────────────────────────────

    public Refinancement create(Long partenaireId, Long banqueId, Refinancement refinancement, String userEmail) {
        Partenaire partenaire = partenaireService.findById(partenaireId);
        if (partenaire.getType() != TypePartenaire.FOURNISSEUR) {
            throw new IllegalArgumentException(
                "Le refinancement est réservé aux partenaires de type FOURNISSEUR");
        }
        Banque banque = banqueService.findById(banqueId);
        refinancement.setPartenaire(partenaire);
        refinancement.setBanque(banque);

        if (banque.getTauxRefinancement() == null) {
            throw new IllegalArgumentException(
                "La banque « " + banque.getNom() + " » n'a pas de taux de refinancement configuré. Veuillez le renseigner dans la fiche banque.");
        }
        refinancement.setTaux(banque.getTauxRefinancement());
        refinancement.calculerInterets();

        Refinancement saved = refinancementRepository.save(refinancement);

        // Référence courte : REF-{année}-{id sur 4 chiffres min}
        saved.setReference(String.format("REF-%d-%04d",
            java.time.LocalDate.now().getYear(), saved.getId()));
        saved = refinancementRepository.save(saved);

        logActionService.logParEmail(userEmail, "CRÉATION", "Refinancement", saved.getId(),
            "Réf : " + saved.getReference() + " — Montant : " + saved.getMontant() + " MAD"
            + " — Fournisseur : " + partenaire.getNom() + " — Banque : " + banque.getNom());
        return saved;
    }

    public Refinancement update(Long id, Refinancement updated) {
        Refinancement existing = findById(id);
        existing.setMontant(updated.getMontant());
        existing.setDuree(updated.getDuree());
        existing.setDateEcheance(updated.getDateEcheance());
        existing.calculerInterets();
        return refinancementRepository.save(existing);
    }

    public Refinancement changerStatut(Long id, StatutOperation statut, String userEmail) {
        Refinancement refinancement = findById(id);
        StatutOperation ancien = refinancement.getStatut();
        refinancement.setStatut(statut);
        Refinancement saved = refinancementRepository.save(refinancement);
        String action = switch (statut) {
            case APPROUVE -> "APPROBATION";
            case REJETE   -> "REJET";
            case ANNULE   -> "ANNULATION";
            default       -> "MODIFICATION";
        };
        logActionService.logParEmail(userEmail, action, "Refinancement", id,
            "Réf : " + refinancement.getReference() + " — Statut : " + ancien + " → " + statut);
        return saved;
    }

    public void delete(Long id, String userEmail) {
        Refinancement r = findById(id);
        logActionService.logParEmail(userEmail, "SUPPRESSION", "Refinancement", id,
            "Réf : " + r.getReference() + " supprimé");
        refinancementRepository.deleteById(id);
    }

    // ─── Agrégats ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BigDecimal getTotalMontantApprouve() {
        return refinancementRepository.sumMontantApprouve();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInteretsApprouve() {
        return refinancementRepository.sumInteretsApprouve();
    }
}
