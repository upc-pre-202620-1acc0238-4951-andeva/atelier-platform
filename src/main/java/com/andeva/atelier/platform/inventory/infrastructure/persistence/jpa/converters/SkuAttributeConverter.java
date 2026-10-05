package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SkuAttributeConverter implements AttributeConverter<Sku, String> {

    @Override
    public String convertToDatabaseColumn(Sku attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public Sku convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return Sku.of(dbData);
    }
}
