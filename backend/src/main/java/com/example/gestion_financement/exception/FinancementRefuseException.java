package com.example.gestion_financement.exception;

import org.springframework.http.HttpStatus;

/**
 * Levée quand la banque refuse une demande de financement
 * (règle métier : montant trop élevé, dossier incomplet, etc.).
 * HTTP 422 Unprocessable Entity : la requête est valide syntaxiquement
 * mais rejetée par une règle métier.
 */
public class FinancementRefuseException extends BusinessException {

    public FinancementRefuseException() {
        super("Demande de financement refusée par la banque.",
              HttpStatus.UNPROCESSABLE_ENTITY,
              "FINANCEMENT_REFUSE");
    }

    public FinancementRefuseException(String raison) {
        super("Demande de financement refusée par la banque : " + raison,
              HttpStatus.UNPROCESSABLE_ENTITY,
              "FINANCEMENT_REFUSE");
    }
}
