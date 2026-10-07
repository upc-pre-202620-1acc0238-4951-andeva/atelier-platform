package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link AlertStatus} domain enumeration.
 * Persists enum values as uppercase strings (DISPATCHED, ACKNOWLEDGED, RESOLVED, DISMISSED).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class AlertStatusConverter implements AttributeConverter<AlertStatus, String> {

    @Override
    public String convertToDatabaseColumn(AlertStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AlertStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return AlertStatus.valueOf(dbData.trim().toUpperCase());
    }
}
