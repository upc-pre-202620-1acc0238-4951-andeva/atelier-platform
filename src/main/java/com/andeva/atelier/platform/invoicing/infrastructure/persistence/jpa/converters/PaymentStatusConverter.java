package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link PaymentStatus} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return PaymentStatus.valueOf(dbData.trim());
    }
}
