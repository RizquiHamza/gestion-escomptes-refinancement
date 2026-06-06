package com.example.gestion_financement.service;

import com.example.gestion_financement.dto.response.DemandeReinitialisationResponse;
import com.example.gestion_financement.entity.DemandeReinitialisation;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.StatutDemande;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.DemandeReinitialisationRepository;
import com.example.gestion_financement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeReinitialisationService {

    private final DemandeReinitialisationRepository demandeRepository;
    private final UtilisateurRepository             utilisateurRepository;
    private final PasswordEncoder                   passwordEncoder;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // ── Soumission d'une demande (côté utilisateur, sans authentification) ────────

    public void soumettre(String email, String message) {
        utilisateurRepository.findByEmail(email).ifPresent(u -> {
            // Annule toute demande en attente existante pour cet utilisateur
            demandeRepository.deleteByUtilisateurAndStatut(u, StatutDemande.EN_ATTENTE);
            demandeRepository.save(DemandeReinitialisation.builder()
                .utilisateur(u)
                .message(message)
                .build());
        });
        // Réponse identique que l'email existe ou non (sécurité anti-énumération)
    }

    // ── Lecture (admin) ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DemandeReinitialisationResponse> findAll() {
        return demandeRepository.findAllByOrderByDateCreationDesc()
            .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DemandeReinitialisationResponse> findEnAttente() {
        return demandeRepository.findByStatutOrderByDateCreationDesc(StatutDemande.EN_ATTENTE)
            .stream().map(this::toDto).toList();
    }

    // ── Approbation (admin) ───────────────────────────────────────────────────────

    public String approuver(Long id) {
        DemandeReinitialisation demande = findById(id);
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée.");
        }

        String tempPassword = genererMotDePasseTemporaire();
        Utilisateur u = demande.getUtilisateur();
        u.setMotDePasse(passwordEncoder.encode(tempPassword));
        u.setDoitChangerMotDePasse(true);
        utilisateurRepository.save(u);

        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateTraitement(LocalDateTime.now());
        demandeRepository.save(demande);

        return tempPassword;
    }

    // ── Refus (admin) ─────────────────────────────────────────────────────────────

    public void refuser(Long id) {
        DemandeReinitialisation demande = findById(id);
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée.");
        }
        demande.setStatut(StatutDemande.REFUSEE);
        demande.setDateTraitement(LocalDateTime.now());
        demandeRepository.save(demande);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────────

    private DemandeReinitialisation findById(Long id) {
        return demandeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }

    private String genererMotDePasseTemporaire() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private DemandeReinitialisationResponse toDto(DemandeReinitialisation d) {
        Utilisateur u = d.getUtilisateur();
        return DemandeReinitialisationResponse.builder()
            .id(d.getId())
            .utilisateurId(u.getId())
            .utilisateurNom(u.getNom())
            .utilisateurPrenom(u.getPrenom())
            .utilisateurEmail(u.getEmail())
            .utilisateurRole(u.getRole().name())
            .statut(d.getStatut())
            .message(d.getMessage())
            .dateCreation(d.getDateCreation())
            .dateTraitement(d.getDateTraitement())
            .build();
    }
}
