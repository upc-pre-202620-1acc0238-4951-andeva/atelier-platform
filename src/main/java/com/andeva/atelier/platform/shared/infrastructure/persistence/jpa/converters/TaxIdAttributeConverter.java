package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for bidirectional mapping between the {@link TaxId}
 * Value Object and a relational {@code VARCHAR(11)} column in PostgreSQL,
 * deducing the tax document type by value length.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = false)
public class TaxIdAttributeConverter implements AttributeConverter<TaxId, String> {

    @Override
    public String convertToDatabaseColumn(TaxId attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.value();
    }

    @Override
    public TaxId convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        String trimmed = dbData.trim();
        TaxIdType type = trimmed.length() == 11 ? TaxIdType.RUC : TaxIdType.DNI;
        return new TaxId(trimmed, type);
    }
}
