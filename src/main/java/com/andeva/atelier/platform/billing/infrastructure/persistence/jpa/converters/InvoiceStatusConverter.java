package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link InvoiceStatus} domain enumeration.
 * Maps between domain enum and lowercase database status values (paid, open, void, uncollectible, draft).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class InvoiceStatusConverter implements AttributeConverter<InvoiceStatus, String> {

    @Override
    public String convertToDatabaseColumn(InvoiceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public InvoiceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return InvoiceStatus.valueOf(dbData.trim().toUpperCase());
    }
}
