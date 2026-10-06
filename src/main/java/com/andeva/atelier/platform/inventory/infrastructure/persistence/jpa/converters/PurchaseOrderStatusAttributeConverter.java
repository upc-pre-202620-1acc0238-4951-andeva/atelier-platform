package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class PurchaseOrderStatusAttributeConverter implements AttributeConverter<PurchaseOrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(PurchaseOrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public PurchaseOrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return PurchaseOrderStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
