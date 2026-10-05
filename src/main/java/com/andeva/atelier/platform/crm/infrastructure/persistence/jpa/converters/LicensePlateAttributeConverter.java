package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class LicensePlateAttributeConverter implements AttributeConverter<LicensePlate, String> {

    @Override
    public String convertToDatabaseColumn(LicensePlate attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public LicensePlate convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return new LicensePlate(dbData);
    }
}
