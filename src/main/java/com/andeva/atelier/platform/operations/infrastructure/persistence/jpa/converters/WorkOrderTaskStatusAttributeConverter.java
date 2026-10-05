package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class WorkOrderTaskStatusAttributeConverter implements AttributeConverter<WorkOrderTaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(WorkOrderTaskStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public WorkOrderTaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return WorkOrderTaskStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
