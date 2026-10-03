package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link BillingCycle} domain enumeration.
 * Persists enum values as uppercase strings (MONTHLY, YEARLY).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class BillingCycleConverter implements AttributeConverter<BillingCycle, String> {

    @Override
    public String convertToDatabaseColumn(BillingCycle attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public BillingCycle convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return BillingCycle.valueOf(dbData.trim().toUpperCase());
    }
}
