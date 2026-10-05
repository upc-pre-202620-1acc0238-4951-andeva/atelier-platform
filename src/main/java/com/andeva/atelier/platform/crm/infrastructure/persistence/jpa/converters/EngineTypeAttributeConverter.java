package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class EngineTypeAttributeConverter implements AttributeConverter<EngineType, String> {

    @Override
    public String convertToDatabaseColumn(EngineType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public EngineType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return EngineType.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
