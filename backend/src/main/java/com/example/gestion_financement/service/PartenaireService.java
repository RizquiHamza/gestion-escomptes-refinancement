package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.TypePartenaire;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.PartenaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartenaireService {

    private final PartenaireRepository partenaireRepository;
    private final LogActionService     logActionService;

    // ─── Lecture paginée ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Partenaire> findAll(Pageable pageable) {
        return partenaireRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Partenaire> findByType(TypePartenaire type, Pageable pageable) {
        return partenaireRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Partenaire> findClients(Pageable pageable) {
        return findByType(TypePartenaire.CLIENT, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Partenaire> findFournisseurs(Pageable pageable) {
        return findByType(TypePartenaire.FOURNISSEUR, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Partenaire> search(String nom, Pageable pageable) {
        return partenaireRepository.findByNomContainingIgnoreCase(nom, pageable);
    }

    // ─── Lecture unitaire (utilisée par EscompteService / RefinancementService) ──

    @Transactional(readOnly = true)
    public Partenaire findById(Long id) {
        return partenaireRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Partenaire non trouvé avec l'id: " + id));
    }

    // ─── Écriture ─────────────────────────────────────────────────────────────────

    public Partenaire save(Partenaire partenaire, String userEmail) {
        if (partenaire.getEmail() != null
                && partenaireRepository.existsByEmail(partenaire.getEmail())) {
            throw new IllegalArgumentException(
                "Un partenaire avec cet email existe déjà: " + partenaire.getEmail());
        }
        Partenaire saved = partenaireRepository.save(partenaire);
        logActionService.logParEmail(userEmail, "CRÉATION", "Partenaire", saved.getId(),
            saved.getType() + " : " + saved.getNom());
        return saved;
    }

    public Partenaire update(Long id, Partenaire updated, String userEmail) {
        Partenaire existing = findById(id);
        if (updated.getEmail() != null
                && !updated.getEmail().equals(existing.getEmail())
                && partenaireRepository.existsByEmail(updated.getEmail())) {
            throw new IllegalArgumentException(
                "Un partenaire avec cet email existe déjà: " + updated.getEmail());
        }
        existing.setNom(updated.getNom());
        existing.setEmail(updated.getEmail());
        existing.setTelephone(updated.getTelephone());
        existing.setAdresse(updated.getAdresse());
        existing.setType(updated.getType());
        Partenaire saved = partenaireRepository.save(existing);
        logActionService.logParEmail(userEmail, "MODIFICATION", "Partenaire", saved.getId(),
            saved.getType() + " : " + saved.getNom() + " — mis à jour");
        return saved;
    }

    public void delete(Long id, String userEmail) {
        Partenaire p = findById(id);
        logActionService.logParEmail(userEmail, "SUPPRESSION", "Partenaire", id,
            p.getType() + " : " + p.getNom() + " supprimé");
        partenaireRepository.deleteById(id);
    }
}
