package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link FaultSeverity} domain enumeration.
 * Persists enum values as uppercase strings (LOW, MEDIUM, CRITICAL).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class FaultSeverityConverter implements AttributeConverter<FaultSeverity, String> {

    @Override
    public String convertToDatabaseColumn(FaultSeverity attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public FaultSeverity convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return FaultSeverity.valueOf(dbData.trim().toUpperCase());
    }
}
