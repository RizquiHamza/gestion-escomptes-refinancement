package com.example.gestion_financement.exception;

import org.springframework.http.HttpStatus;

/**
 * Classe de base pour toutes les exceptions métier de l'application.
 * Chaque sous-classe définit son propre message, statut HTTP et code d'erreur.
 */
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String     code;

    protected BusinessException(String message, HttpStatus httpStatus, String code) {
        super(message);
        this.httpStatus = httpStatus;
        this.code       = code;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String     getCode()       { return code; }
}
