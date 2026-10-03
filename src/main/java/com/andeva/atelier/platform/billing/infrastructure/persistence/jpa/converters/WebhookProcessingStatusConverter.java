package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA attribute converter for {@link WebhookProcessingStatus} domain enumeration.
 * Maps between domain enum and lowercase database status values (pending, processed, failed, ignored).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class WebhookProcessingStatusConverter implements AttributeConverter<WebhookProcessingStatus, String> {

    @Override
    public String convertToDatabaseColumn(WebhookProcessingStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public WebhookProcessingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return WebhookProcessingStatus.valueOf(dbData.trim().toUpperCase());
    }
}
