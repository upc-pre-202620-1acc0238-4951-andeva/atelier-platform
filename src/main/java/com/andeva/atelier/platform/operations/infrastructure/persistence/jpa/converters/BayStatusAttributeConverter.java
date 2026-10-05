package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class BayStatusAttributeConverter implements AttributeConverter<BayStatus, String> {

    @Override
    public String convertToDatabaseColumn(BayStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public BayStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return BayStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
