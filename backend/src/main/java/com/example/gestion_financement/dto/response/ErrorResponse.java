package com.example.gestion_financement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Réponse JSON uniforme pour toutes les erreurs de l'API.
 * Les champs null (ex. "code" pour les erreurs génériques) sont omis du JSON.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String        timestamp;
    private int           status;
    private String        error;
    private String        code;
    private String        message;

    /** Fabrique une ErrorResponse à partir d'un HttpStatus, un message et un code métier. */
    public static ErrorResponse of(HttpStatus httpStatus, String message, String code) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .code(code)
                .message(message)
                .build();
    }

    /** Fabrique une ErrorResponse sans code métier (erreurs techniques). */
    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .build();
    }
}
