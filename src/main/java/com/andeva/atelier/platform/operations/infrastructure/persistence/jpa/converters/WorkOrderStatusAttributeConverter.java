package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class WorkOrderStatusAttributeConverter implements AttributeConverter<WorkOrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(WorkOrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public WorkOrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return WorkOrderStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
