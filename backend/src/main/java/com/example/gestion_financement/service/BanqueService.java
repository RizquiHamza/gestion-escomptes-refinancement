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

    public Banque save(Banque banque) {
        if (banque.getCode() != null && banqueRepository.existsByCode(banque.getCode())) {
            throw new IllegalArgumentException(
                "Une banque avec ce code existe déjà: " + banque.getCode());
        }
        if (banqueRepository.existsByNom(banque.getNom())) {
            throw new IllegalArgumentException(
                "Une banque avec ce nom existe déjà: " + banque.getNom());
        }
        return banqueRepository.save(banque);
    }

    public Banque update(Long id, Banque updated) {
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
        return banqueRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        banqueRepository.deleteById(id);
    }
}
