package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttendanceStatusAttributeConverter implements AttributeConverter<AttendanceStatus, String> {

    @Override
    public String convertToDatabaseColumn(AttendanceStatus attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase();
    }

    @Override
    public AttendanceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return AttendanceStatus.valueOf(dbData.trim().toUpperCase());
    }
}
