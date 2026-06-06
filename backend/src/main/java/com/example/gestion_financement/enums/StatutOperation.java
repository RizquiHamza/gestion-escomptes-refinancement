package com.example.gestion_financement.enums;

public enum StatutOperation {
    EN_ATTENTE,
    APPROUVE,
    REJETE,
    ANNULE,
    CLOS,
    REFINANCE   // conservé pour compatibilité base de données — non exposé au frontend
}
