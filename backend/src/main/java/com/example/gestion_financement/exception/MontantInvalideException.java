package com.example.gestion_financement.exception;

import org.springframework.http.HttpStatus;

/**
 * Levée quand un montant est inférieur ou égal à zéro.
 * HTTP 400 Bad Request : entrée invalide côté client.
 */
public class MontantInvalideException extends BusinessException {

    public MontantInvalideException() {
        super("Le montant doit être supérieur à 0.",
              HttpStatus.BAD_REQUEST,
              "MONTANT_INVALIDE");
    }

    public MontantInvalideException(String detail) {
        super("Le montant doit être supérieur à 0. " + detail,
              HttpStatus.BAD_REQUEST,
              "MONTANT_INVALIDE");
    }
}
