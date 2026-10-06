package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.hr.domain.model.enums.PayrollStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PayrollStatusAttributeConverter implements AttributeConverter<PayrollStatus, String> {

    @Override
    public String convertToDatabaseColumn(PayrollStatus attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public PayrollStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return PayrollStatus.valueOf(dbData.trim().toUpperCase());
    }
}
