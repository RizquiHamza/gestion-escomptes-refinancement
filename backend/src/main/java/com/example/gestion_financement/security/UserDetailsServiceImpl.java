package com.example.gestion_financement.security;

import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec l'email : " + email));

        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            throw new UsernameNotFoundException("Compte désactivé : " + email);
        }

        // Spring Security attend le préfixe ROLE_ pour hasRole()
        String roleAuthority = "ROLE_" + utilisateur.getRole().name();

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                List.of(new SimpleGrantedAuthority(roleAuthority))
        );
    }
}
