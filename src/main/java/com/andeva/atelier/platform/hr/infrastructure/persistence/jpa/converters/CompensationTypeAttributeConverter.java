package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CompensationTypeAttributeConverter implements AttributeConverter<CompensationType, String> {

    @Override
    public String convertToDatabaseColumn(CompensationType attribute) {
        if (attribute == null) return null;
        return switch (attribute) {
            case MONTHLY_FIXED -> "monthly_fixed";
            case HOURLY_RATE -> "hourly_rate";
            case DAILY_RATE -> "daily_rate";
            case COMMISSION_BASED -> "commission_based";
        };
    }

    @Override
    public CompensationType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        return switch (dbData.trim().toLowerCase()) {
            case "monthly_fixed" -> CompensationType.MONTHLY_FIXED;
            case "hourly_rate" -> CompensationType.HOURLY_RATE;
            case "daily_rate" -> CompensationType.DAILY_RATE;
            case "commission_based" -> CompensationType.COMMISSION_BASED;
            default -> CompensationType.valueOf(dbData.trim().toUpperCase());
        };
    }
}
