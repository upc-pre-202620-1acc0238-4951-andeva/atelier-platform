package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link DtcCategory} domain enumeration.
 * Persists enum values as uppercase strings (POWERTRAIN_P, CHASSIS_C, BODY_B, NETWORK_U).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class DtcCategoryConverter implements AttributeConverter<DtcCategory, String> {

    @Override
    public String convertToDatabaseColumn(DtcCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public DtcCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return DtcCategory.valueOf(dbData.trim().toUpperCase());
    }
}
