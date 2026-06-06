package com.example.gestion_financement.repository;

import com.example.gestion_financement.entity.DemandeReinitialisation;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeReinitialisationRepository extends JpaRepository<DemandeReinitialisation, Long> {

    List<DemandeReinitialisation> findAllByOrderByDateCreationDesc();

    List<DemandeReinitialisation> findByStatutOrderByDateCreationDesc(StatutDemande statut);

    void deleteByUtilisateurAndStatut(Utilisateur utilisateur, StatutDemande statut);

    boolean existsByUtilisateurAndStatut(Utilisateur utilisateur, StatutDemande statut);
}
