package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class ProposalStatusAttributeConverter implements AttributeConverter<ProposalStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProposalStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ProposalStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return ProposalStatus.valueOf(dbData.trim().toUpperCase(Locale.ROOT));
    }
}
