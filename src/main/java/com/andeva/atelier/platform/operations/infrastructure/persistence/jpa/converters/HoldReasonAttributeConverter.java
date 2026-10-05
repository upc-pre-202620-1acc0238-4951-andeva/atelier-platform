package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class HoldReasonAttributeConverter implements AttributeConverter<HoldReason, String> {

    @Override
    public String convertToDatabaseColumn(HoldReason attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public HoldReason convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return HoldReason.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
