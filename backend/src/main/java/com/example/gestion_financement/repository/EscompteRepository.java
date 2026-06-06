package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Escompte;
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
public interface EscompteRepository extends JpaRepository<Escompte, Long> {

    // ─── Paginés (utilisés par les controllers) ──────────────────────────────────
    Page<Escompte> findByStatut(StatutOperation statut, Pageable pageable);
    Page<Escompte> findByPartenaireId(Long partenaireId, Pageable pageable);
    Page<Escompte> findByBanqueId(Long banqueId, Pageable pageable);

    // ─── Non paginés (usages internes) ───────────────────────────────────────────
    List<Escompte> findByStatut(StatutOperation statut);
    List<Escompte> findByPartenaireId(Long partenaireId);
    List<Escompte> findByBanqueId(Long banqueId);
    Optional<Escompte> findByReference(String reference);

    // ─── Agrégats (Dashboard) ────────────────────────────────────────────────────
    long countByStatut(StatutOperation statut);

    @Query("SELECT SUM(e.montant) FROM Escompte e WHERE e.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumMontantApprouve();

    @Query("SELECT SUM(e.agios) FROM Escompte e WHERE e.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumAgiosApprouve();

    @Query("SELECT SUM(e.netRecu) FROM Escompte e WHERE e.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE")
    BigDecimal sumNetRecuApprouve();

    // ─── Agrégats (Statistiques) ─────────────────────────────────────────────────

    @Query("SELECT e.banque.nom, COUNT(e), SUM(e.montant), SUM(e.agios) " +
           "FROM Escompte e WHERE e.statut = com.example.gestion_financement.enums.StatutOperation.APPROUVE " +
           "GROUP BY e.banque.nom ORDER BY SUM(e.montant) DESC")
    List<Object[]> statsParBanque();

    @Query(value = "SELECT YEAR(date_creation) AS y, MONTH(date_creation) AS m, " +
                   "COUNT(*) AS n, SUM(montant) AS total " +
                   "FROM escompte WHERE date_creation >= :depuis " +
                   "GROUP BY YEAR(date_creation), MONTH(date_creation) ORDER BY y, m",
           nativeQuery = true)
    List<Object[]> evolutionMensuelle(@Param("depuis") LocalDate depuis);
}
