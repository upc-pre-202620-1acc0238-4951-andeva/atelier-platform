package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link SubscriptionStatus} domain enumeration.
 * Maps between domain enum and lowercase database status values (trialing, active, past_due, canceled, unpaid, incomplete).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class SubscriptionStatusConverter implements AttributeConverter<SubscriptionStatus, String> {

    @Override
    public String convertToDatabaseColumn(SubscriptionStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public SubscriptionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return SubscriptionStatus.valueOf(dbData.trim().toUpperCase());
    }
}
