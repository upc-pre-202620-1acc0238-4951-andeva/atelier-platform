package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class VinAttributeConverter implements AttributeConverter<Vin, String> {

    @Override
    public String convertToDatabaseColumn(Vin attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public Vin convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return new Vin(dbData);
    }
}
