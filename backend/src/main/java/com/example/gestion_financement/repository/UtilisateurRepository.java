package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.RoleUtilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // ─── Paginés (utilisés par les controllers) ──────────────────────────────────
    Page<Utilisateur> findByRole(RoleUtilisateur role, Pageable pageable);

    // ─── Non paginés (usages internes / sécurité) ────────────────────────────────
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Utilisateur> findByRole(RoleUtilisateur role);
    List<Utilisateur> findByActif(Boolean actif);
}
