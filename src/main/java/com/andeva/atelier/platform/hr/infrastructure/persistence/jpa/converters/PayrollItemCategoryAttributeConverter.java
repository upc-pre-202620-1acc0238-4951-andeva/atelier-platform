package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.hr.domain.model.enums.PayrollItemCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PayrollItemCategoryAttributeConverter implements AttributeConverter<PayrollItemCategory, String> {

    @Override
    public String convertToDatabaseColumn(PayrollItemCategory attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public PayrollItemCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return PayrollItemCategory.valueOf(dbData.trim().toUpperCase());
    }
}
