package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class ProposalSeverityAttributeConverter implements AttributeConverter<ProposalSeverity, String> {

    @Override
    public String convertToDatabaseColumn(ProposalSeverity attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ProposalSeverity convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return ProposalSeverity.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
