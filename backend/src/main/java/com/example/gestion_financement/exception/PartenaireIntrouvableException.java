package com.example.gestion_financement.exception;

import org.springframework.http.HttpStatus;

/**
 * Levée quand un partenaire est demandé mais absent de la base de données.
 * HTTP 404 Not Found.
 */
public class PartenaireIntrouvableException extends BusinessException {

    public PartenaireIntrouvableException(Long id) {
        super("Partenaire introuvable avec l'identifiant : " + id,
              HttpStatus.NOT_FOUND,
              "PARTENAIRE_INTROUVABLE");
    }

    public PartenaireIntrouvableException() {
        super("Partenaire introuvable.",
              HttpStatus.NOT_FOUND,
              "PARTENAIRE_INTROUVABLE");
    }
}
