package com.example.gestion_financement.config;

import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.enums.RoleUtilisateur;
import com.example.gestion_financement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Crée l'admin par défaut s'il n'existe pas encore (indépendamment des autres utilisateurs)
        if (!utilisateurRepository.existsByEmail("admin@gf.ma")) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Admin")
                    .prenom("Super")
                    .email("admin@gf.ma")
                    .motDePasse(passwordEncoder.encode("admin123"))
                    .role(RoleUtilisateur.ADMIN)
                    .actif(true)
                    .build();

            utilisateurRepository.save(admin);
            log.info("========================================================");
            log.info("  Compte admin créé avec succès !");
            log.info("  Email    : admin@gf.ma");
            log.info("  Password : admin123");
            log.info("========================================================");
        } else {
            log.info("=== Compte admin déjà existant — aucune création nécessaire ===");
        }
    }
}
