package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class InventoryItemStatusAttributeConverter implements AttributeConverter<InventoryItemStatus, String> {

    @Override
    public String convertToDatabaseColumn(InventoryItemStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public InventoryItemStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return InventoryItemStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
