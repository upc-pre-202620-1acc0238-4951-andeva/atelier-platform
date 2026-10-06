package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class ItemCategoryAttributeConverter implements AttributeConverter<ItemCategory, String> {

    @Override
    public String convertToDatabaseColumn(ItemCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ItemCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return ItemCategory.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
