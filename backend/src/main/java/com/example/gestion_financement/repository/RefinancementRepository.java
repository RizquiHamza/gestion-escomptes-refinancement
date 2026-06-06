package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Refinancement;
import com.example.gestion_financement.enums.StatutOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Query("SELECT SUM(r.totalRemboursement) FROM Refinancement r WHERE r.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumTotalRemboursementApprouve();

    // ─── Agrégats (Statistiques) ─────────────────────────────────────────────────

    @Query("SELECT r.banque.nom, COUNT(r), SUM(r.montant), SUM(r.interets) " +
           "FROM Refinancement r WHERE r.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE " +
           "GROUP BY r.banque.nom ORDER BY SUM(r.montant) DESC")
    List<Object[]> statsParBanque();

    @Query(value = "SELECT YEAR(date_creation) AS y, MONTH(date_creation) AS m, " +
                   "COUNT(*) AS n, SUM(montant) AS total " +
                   "FROM refinancement WHERE date_creation >= :depuis " +
                   "GROUP BY YEAR(date_creation), MONTH(date_creation) ORDER BY y, m",
           nativeQuery = true)
    List<Object[]> evolutionMensuelle(@Param("depuis") LocalDate depuis);
}
