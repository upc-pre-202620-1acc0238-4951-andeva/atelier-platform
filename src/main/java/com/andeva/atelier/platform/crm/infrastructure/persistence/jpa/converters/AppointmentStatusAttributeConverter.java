package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class AppointmentStatusAttributeConverter implements AttributeConverter<AppointmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AppointmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public AppointmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return AppointmentStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
