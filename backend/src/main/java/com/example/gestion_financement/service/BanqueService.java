package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Banque;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.BanqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BanqueService {

    private final BanqueRepository banqueRepository;
    private final LogActionService logActionService;

    // ─── Lecture paginée ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Banque> findAll(Pageable pageable) {
        return banqueRepository.findAll(pageable);
    }

    // ─── Lecture unitaire (utilisée par EscompteService / RefinancementService) ──

    @Transactional(readOnly = true)
    public Banque findById(Long id) {
        return banqueRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Banque non trouvée avec l'id: " + id));
    }

    // ─── Écriture ─────────────────────────────────────────────────────────────────

    public Banque save(Banque banque, String userEmail) {
        if (banque.getCode() != null && banqueRepository.existsByCode(banque.getCode())) {
            throw new IllegalArgumentException(
                "Une banque avec ce code existe déjà: " + banque.getCode());
        }
        if (banqueRepository.existsByNom(banque.getNom())) {
            throw new IllegalArgumentException(
                "Une banque avec ce nom existe déjà: " + banque.getNom());
        }
        Banque saved = banqueRepository.save(banque);
        logActionService.logParEmail(userEmail, "CRÉATION", "Banque", saved.getId(),
            "Banque : " + saved.getNom() + (saved.getCode() != null ? " (code : " + saved.getCode() + ")" : ""));
        return saved;
    }

    public Banque update(Long id, Banque updated, String userEmail) {
        Banque existing = findById(id);
        if (updated.getCode() != null
                && !updated.getCode().equals(existing.getCode())
                && banqueRepository.existsByCode(updated.getCode())) {
            throw new IllegalArgumentException(
                "Une banque avec ce code existe déjà: " + updated.getCode());
        }
        if (!updated.getNom().equals(existing.getNom())
                && banqueRepository.existsByNom(updated.getNom())) {
            throw new IllegalArgumentException(
                "Une banque avec ce nom existe déjà: " + updated.getNom());
        }
        existing.setNom(updated.getNom());
        existing.setCode(updated.getCode());
        existing.setAdresse(updated.getAdresse());
        existing.setTelephone(updated.getTelephone());
        existing.setTauxEscompte(updated.getTauxEscompte());
        existing.setTauxRefinancement(updated.getTauxRefinancement());
        Banque saved = banqueRepository.save(existing);
        logActionService.logParEmail(userEmail, "MODIFICATION", "Banque", saved.getId(),
            "Banque : " + saved.getNom() + " — mise à jour");
        return saved;
    }

    public void delete(Long id, String userEmail) {
        Banque b = findById(id);
        logActionService.logParEmail(userEmail, "SUPPRESSION", "Banque", id,
            "Banque : " + b.getNom() + " supprimée");
        banqueRepository.deleteById(id);
    }
}
