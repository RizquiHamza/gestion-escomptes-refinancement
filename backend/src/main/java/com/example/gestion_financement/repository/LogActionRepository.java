package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.LogAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {

    List<LogAction> findByUtilisateurId(Long utilisateurId);

    List<LogAction> findByEntite(String entite);

    List<LogAction> findByEntiteAndEntiteId(String entite, Long entiteId);

    List<LogAction> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin);

    List<LogAction> findTop100ByOrderByDateActionDesc();
}
