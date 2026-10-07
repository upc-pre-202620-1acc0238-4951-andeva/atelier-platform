package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link DeviceStatus} domain enumeration.
 * Persists enum values as uppercase strings (ACTIVE, INACTIVE, LOST, BROKEN).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class DeviceStatusConverter implements AttributeConverter<DeviceStatus, String> {

    @Override
    public String convertToDatabaseColumn(DeviceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public DeviceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return DeviceStatus.valueOf(dbData.trim().toUpperCase());
    }
}
