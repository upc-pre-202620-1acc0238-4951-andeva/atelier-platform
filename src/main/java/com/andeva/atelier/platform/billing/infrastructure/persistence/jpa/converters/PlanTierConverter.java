package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link PlanTier} domain enumeration.
 * Persists enum values as uppercase strings (GO, PRO, MAX, ENTERPRISE).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class PlanTierConverter implements AttributeConverter<PlanTier, String> {

    @Override
    public String convertToDatabaseColumn(PlanTier attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public PlanTier convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return PlanTier.valueOf(dbData.trim().toUpperCase());
    }
}
