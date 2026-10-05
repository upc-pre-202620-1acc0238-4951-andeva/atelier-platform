package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class CustomerTypeAttributeConverter implements AttributeConverter<CustomerType, String> {

    @Override
    public String convertToDatabaseColumn(CustomerType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public CustomerType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return CustomerType.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
