package com.example.gestion_financement.service;

import com.example.gestion_financement.entity.LogAction;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.repository.LogActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LogActionService {

    private final LogActionRepository logActionRepository;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<LogAction> findAll() {
        return logActionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LogAction> findRecents() {
        return logActionRepository.findTop100ByOrderByDateActionDesc();
    }

    @Transactional(readOnly = true)
    public List<LogAction> findByUtilisateur(Long utilisateurId) {
        return logActionRepository.findByUtilisateurId(utilisateurId);
    }

    @Transactional(readOnly = true)
    public List<LogAction> findByEntite(String entite) {
        return logActionRepository.findByEntite(entite);
    }

    @Transactional(readOnly = true)
    public List<LogAction> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        return logActionRepository.findByDateActionBetween(debut, fin);
    }

    public LogAction log(Long utilisateurId, String action, String entite,
                         Long entiteId, String details) {
        Utilisateur utilisateur = utilisateurService.findById(utilisateurId);
        LogAction log = LogAction.builder()
            .utilisateur(utilisateur)
            .action(action)
            .entite(entite)
            .entiteId(entiteId)
            .details(details)
            .dateAction(LocalDateTime.now())
            .build();
        return logActionRepository.save(log);
    }
}
