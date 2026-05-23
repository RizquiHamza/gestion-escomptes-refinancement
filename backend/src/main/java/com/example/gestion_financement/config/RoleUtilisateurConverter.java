package com.example.gestion_financement.config;

import com.example.gestion_financement.enums.RoleUtilisateur;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleUtilisateurConverter implements AttributeConverter<RoleUtilisateur, String> {

    @Override
    public String convertToDatabaseColumn(RoleUtilisateur role) {
        return role == null ? null : role.name();
    }

    @Override
    public RoleUtilisateur convertToEntityAttribute(String value) {
        if (value == null) return null;
        return RoleUtilisateur.valueOf(value.toUpperCase());
    }
}
