package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertSeverity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link AlertSeverity} domain enumeration.
 * Persists enum values as uppercase strings (LOW, MEDIUM, HIGH, CRITICAL).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class AlertSeverityConverter implements AttributeConverter<AlertSeverity, String> {

    @Override
    public String convertToDatabaseColumn(AlertSeverity attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AlertSeverity convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return AlertSeverity.valueOf(dbData.trim().toUpperCase());
    }
}
