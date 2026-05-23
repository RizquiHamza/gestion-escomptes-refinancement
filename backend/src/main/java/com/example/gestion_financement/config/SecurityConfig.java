package com.example.gestion_financement.config;

import com.example.gestion_financement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation CSRF (API stateless JWT, pas de session)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS : utilise automatiquement le bean CorsConfigurationSource déclaré dans CorsConfig
            .cors(Customizer.withDefaults())

            // Session stateless — aucune session HTTP créée
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Règles d'autorisation par endpoint
            .authorizeHttpRequests(auth -> auth

                // Routes publiques : login, register + Swagger UI
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Gestion des utilisateurs → ADMIN uniquement
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")

                // Dashboard et logs → ADMIN et RESPONSABLE
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "RESPONSABLE")
                .requestMatchers("/api/logs/**").hasAnyRole("ADMIN", "RESPONSABLE")

                // Suppressions sensibles → ADMIN et RESPONSABLE uniquement
                .requestMatchers(HttpMethod.DELETE, "/api/escomptes/**")
                    .hasAnyRole("ADMIN", "RESPONSABLE")
                .requestMatchers(HttpMethod.DELETE, "/api/refinancements/**")
                    .hasAnyRole("ADMIN", "RESPONSABLE")
                .requestMatchers(HttpMethod.DELETE, "/api/partenaires/**")
                    .hasAnyRole("ADMIN", "RESPONSABLE")
                .requestMatchers(HttpMethod.DELETE, "/api/banques/**")
                    .hasRole("ADMIN")

                // Changement de statut → ADMIN et RESPONSABLE
                .requestMatchers(HttpMethod.PATCH, "/api/escomptes/*/statut")
                    .hasAnyRole("ADMIN", "RESPONSABLE")
                .requestMatchers(HttpMethod.PATCH, "/api/refinancements/*/statut")
                    .hasAnyRole("ADMIN", "RESPONSABLE")

                // Tout le reste → authentifié (tous les rôles)
                .anyRequest().authenticated()
            )

            // Fournisseur d'authentification DAO (email + mot de passe hashé)
            .authenticationProvider(authenticationProvider)

            // Filtre JWT avant le filtre username/password standard de Spring Security
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
