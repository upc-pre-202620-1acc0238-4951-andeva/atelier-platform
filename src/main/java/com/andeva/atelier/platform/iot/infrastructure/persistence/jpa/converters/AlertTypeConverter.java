package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link AlertType} domain enumeration.
 * Persists enum values as uppercase strings (ENGINE_OVERHEATING_RISK, BATTERY_FAILURE_RISK, etc.).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class AlertTypeConverter implements AttributeConverter<AlertType, String> {

    @Override
    public String convertToDatabaseColumn(AlertType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AlertType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return AlertType.valueOf(dbData.trim().toUpperCase());
    }
}
