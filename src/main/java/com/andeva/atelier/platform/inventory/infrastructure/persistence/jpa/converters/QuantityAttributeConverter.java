package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter(autoApply = false)
public class QuantityAttributeConverter implements AttributeConverter<Quantity, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Quantity attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public Quantity convertToEntityAttribute(BigDecimal dbData) {
        if (dbData == null) {
            return null;
        }
        return Quantity.of(dbData);
    }
}
