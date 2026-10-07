package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link ConnectionType} domain enumeration.
 * Persists enum values as uppercase strings (BLUETOOTH_BLE, SIM_CELLULAR, WIFI).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class ConnectionTypeConverter implements AttributeConverter<ConnectionType, String> {

    @Override
    public String convertToDatabaseColumn(ConnectionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ConnectionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return ConnectionType.valueOf(dbData.trim().toUpperCase());
    }
}
