package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.Escompte;
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
}
