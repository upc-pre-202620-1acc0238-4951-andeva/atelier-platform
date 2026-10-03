package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Mileage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for bidirectional mapping between the {@link Mileage}
 * Value Object and a relational {@code INTEGER} column in PostgreSQL.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = false)
public class MileageAttributeConverter implements AttributeConverter<Mileage, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Mileage attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public Mileage convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return new Mileage(dbData);
    }
}
