package com.example.gestion_financement.controller;

import com.example.gestion_financement.dto.request.AuthRequest;
import com.example.gestion_financement.dto.request.RegisterRequest;
import com.example.gestion_financement.dto.response.AuthResponse;
import com.example.gestion_financement.entity.Utilisateur;
import com.example.gestion_financement.exception.ResourceNotFoundException;
import com.example.gestion_financement.repository.UtilisateurRepository;
import com.example.gestion_financement.security.JwtUtil;
import com.example.gestion_financement.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentification", description = "Connexion et création de compte")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Operation(summary = "Se connecter et obtenir un token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connexion réussie, token JWT retourné"),
        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé : " + request.getEmail()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(buildExtraClaims(utilisateur), userDetails);

        return ResponseEntity.ok(buildAuthResponse(utilisateur, token));
    }

    @Operation(summary = "Créer un nouveau compte utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Compte créé, token JWT retourné"),
        @ApiResponse(responseCode = "400", description = "Email déjà utilisé ou données invalides")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Un utilisateur avec cet email existe déjà : " + request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(request.getRole())
                .actif(true)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());
        String token = jwtUtil.generateToken(buildExtraClaims(utilisateur), userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(utilisateur, token));
    }

    private Map<String, Object> buildExtraClaims(Utilisateur utilisateur) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", utilisateur.getRole().name());
        claims.put("nom", utilisateur.getNom());
        claims.put("prenom", utilisateur.getPrenom());
        return claims;
    }

    private AuthResponse buildAuthResponse(Utilisateur utilisateur, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .utilisateurId(utilisateur.getId())
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .role(utilisateur.getRole().name())
                .expiresIn(jwtUtil.getJwtExpiration())
                .build();
    }
}
