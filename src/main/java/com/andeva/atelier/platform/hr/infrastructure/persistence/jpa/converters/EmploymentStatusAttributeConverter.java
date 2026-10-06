package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmploymentStatusAttributeConverter implements AttributeConverter<EmploymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(EmploymentStatus attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public EmploymentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return EmploymentStatus.valueOf(dbData.trim().toUpperCase());
    }
}
