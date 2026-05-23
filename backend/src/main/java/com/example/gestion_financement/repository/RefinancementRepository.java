package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Refinancement;
import com.example.gestion_financement.enums.StatutOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefinancementRepository extends JpaRepository<Refinancement, Long> {

    // ─── Paginés (utilisés par les controllers) ──────────────────────────────────
    Page<Refinancement> findByStatut(StatutOperation statut, Pageable pageable);
    Page<Refinancement> findByPartenaireId(Long partenaireId, Pageable pageable);
    Page<Refinancement> findByBanqueId(Long banqueId, Pageable pageable);

    // ─── Non paginés (usages internes) ───────────────────────────────────────────
    List<Refinancement> findByStatut(StatutOperation statut);
    List<Refinancement> findByPartenaireId(Long partenaireId);
    List<Refinancement> findByBanqueId(Long banqueId);
    Optional<Refinancement> findByReference(String reference);

    // ─── Agrégats (Dashboard) ────────────────────────────────────────────────────
    long countByStatut(StatutOperation statut);

    @Query("SELECT SUM(r.montant) FROM Refinancement r WHERE r.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumMontantApprouve();

    @Query("SELECT SUM(r.interets) FROM Refinancement r WHERE r.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumInteretsApprouve();
}
