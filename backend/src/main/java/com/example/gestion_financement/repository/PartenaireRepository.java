package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Partenaire;
import com.example.gestion_financement.enums.TypePartenaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartenaireRepository extends JpaRepository<Partenaire, Long> {

    // ─── Paginés (utilisés par les controllers) ──────────────────────────────────
    Page<Partenaire> findByType(TypePartenaire type, Pageable pageable);
    Page<Partenaire> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    // ─── Non paginés (usages internes) ───────────────────────────────────────────
    List<Partenaire> findByType(TypePartenaire type);
    Optional<Partenaire> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Partenaire> findByNomContainingIgnoreCase(String nom);

    // ─── Agrégats (Dashboard) ────────────────────────────────────────────────────
    long countByType(TypePartenaire type);
}
