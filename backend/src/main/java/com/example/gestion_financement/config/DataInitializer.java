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
        // Migration : renommer les anciens emails @gf.ma → @unimagec.ma s'ils existent encore
        migrerEmail("admin@gf.ma",        "admin@unimagec.ma");
        migrerEmail("responsable@gf.ma",  "responsable@unimagec.ma");
        migrerEmail("agent@gf.ma",        "agent@unimagec.ma");

        // Créer ou réinitialiser les comptes avec les nouvelles adresses
        creerOuResetUser("admin@unimagec.ma",        "Admin",   "Super",   "admin123",        RoleUtilisateur.ADMIN);
        creerOuResetUser("responsable@unimagec.ma",  "Benali",  "Fatima",  "responsable123",  RoleUtilisateur.RESPONSABLE);
        creerOuResetUser("agent@unimagec.ma",        "Alami",   "Mohamed", "agent123",        RoleUtilisateur.AGENT_FINANCIER);

        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║              COMPTES UNIMAGEC DISPONIBLES                   ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║  ADMIN        : admin@unimagec.ma        / admin123         ║");
        log.info("║  RESPONSABLE  : responsable@unimagec.ma  / responsable123   ║");
        log.info("║  AGENT        : agent@unimagec.ma        / agent123         ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }

    private void migrerEmail(String ancienEmail, String nouvelEmail) {
        utilisateurRepository.findByEmail(ancienEmail).ifPresent(u -> {
            u.setEmail(nouvelEmail);
            utilisateurRepository.save(u);
            log.info("  [MIGRATION] {} → {}", ancienEmail, nouvelEmail);
        });
    }

    private void creerOuResetUser(String email, String nom, String prenom,
                                   String motDePasse, RoleUtilisateur role) {
        utilisateurRepository.findByEmail(email).ifPresentOrElse(
            u -> {
                u.setMotDePasse(passwordEncoder.encode(motDePasse));
                u.setActif(true);
                u.setRole(role);
                utilisateurRepository.save(u);
                log.info("  [RESET] {} ({})", email, role.name());
            },
            () -> {
                Utilisateur u = Utilisateur.builder()
                        .nom(nom)
                        .prenom(prenom)
                        .email(email)
                        .motDePasse(passwordEncoder.encode(motDePasse))
                        .role(role)
                        .actif(true)
                        .build();
                utilisateurRepository.save(u);
                log.info("  [CRÉÉ]  {} ({})", email, role.name());
            }
        );
    }
}
