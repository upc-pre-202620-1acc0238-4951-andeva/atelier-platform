package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class EvidenceTypeAttributeConverter implements AttributeConverter<EvidenceType, String> {

    @Override
    public String convertToDatabaseColumn(EvidenceType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public EvidenceType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return EvidenceType.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
