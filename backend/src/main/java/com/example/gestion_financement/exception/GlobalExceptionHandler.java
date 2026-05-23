package com.example.gestion_financement.exception;

import com.example.gestion_financement.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions — intercepte toutes les exceptions
 * lancées par les controllers et les transforme en réponses JSON structurées.
 *
 * Ordre de priorité : les handlers les plus spécifiques sont déclarés en premier.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Exceptions métier (BusinessException et sous-classes) ───────────────────

    /**
     * Gère toutes les exceptions métier : FinancementRefuseException,
     * PartenaireIntrouvableException, MontantInvalideException, etc.
     * Le statut HTTP et le code sont portés par l'exception elle-même.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ErrorResponse.of(ex.getHttpStatus(), ex.getMessage(), ex.getCode()));
    }

    // ─── Ressource introuvable (générique) ────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    // ─── Erreurs de validation Bean Validation (@Valid) ───────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST,
                        "Validation échouée : " + details, "VALIDATION_FAILED"));
    }

    // ─── Argument illégal (règles basiques) ───────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT"));
    }

    // ─── Authentification ─────────────────────────────────────────────────────────

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> handleAuthError(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED,
                        "Email ou mot de passe incorrect", "AUTH_FAILED"));
    }

    // ─── Erreur générique (fallback) ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erreur interne du serveur : " + ex.getMessage()));
    }
}
