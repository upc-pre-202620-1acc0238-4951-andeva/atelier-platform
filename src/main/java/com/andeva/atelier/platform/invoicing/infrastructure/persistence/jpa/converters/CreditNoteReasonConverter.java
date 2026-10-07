package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link CreditNoteReason} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class CreditNoteReasonConverter implements AttributeConverter<CreditNoteReason, String> {

    @Override
    public String convertToDatabaseColumn(CreditNoteReason attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public CreditNoteReason convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return CreditNoteReason.valueOf(dbData.trim());
    }
}
