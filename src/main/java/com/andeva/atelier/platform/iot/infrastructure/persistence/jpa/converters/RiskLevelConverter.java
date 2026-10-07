package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.RiskLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link RiskLevel} domain enumeration.
 * Persists enum values as uppercase strings (LOW, MODERATE, HIGH, CRITICAL).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class RiskLevelConverter implements AttributeConverter<RiskLevel, String> {

    @Override
    public String convertToDatabaseColumn(RiskLevel attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public RiskLevel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return RiskLevel.valueOf(dbData.trim().toUpperCase());
    }
}
