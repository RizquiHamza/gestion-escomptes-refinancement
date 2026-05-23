package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.RoleUtilisateur;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Lecture paginée ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Utilisateur> findAll(Pageable pageable) {
        return utilisateurRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Utilisateur> findByRole(RoleUtilisateur role, Pageable pageable) {
        return utilisateurRepository.findByRole(role, pageable);
    }

    // ─── Lecture unitaire ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id));
    }

    @Transactional(readOnly = true)
    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    // ─── Écriture ─────────────────────────────────────────────────────────────────

    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new IllegalArgumentException(
                "Un utilisateur avec cet email existe déjà: " + utilisateur.getEmail());
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur update(Long id, Utilisateur updated) {
        Utilisateur existing = findById(id);
        existing.setNom(updated.getNom());
        existing.setPrenom(updated.getPrenom());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        if (updated.getMotDePasse() != null && !updated.getMotDePasse().isBlank()) {
            existing.setMotDePasse(passwordEncoder.encode(updated.getMotDePasse()));
        }
        return utilisateurRepository.save(existing);
    }

    public Utilisateur toggleActif(Long id) {
        Utilisateur utilisateur = findById(id);
        utilisateur.setActif(!utilisateur.getActif());
        return utilisateurRepository.save(utilisateur);
    }

    public void delete(Long id) {
        findById(id);
        utilisateurRepository.deleteById(id);
    }
}
