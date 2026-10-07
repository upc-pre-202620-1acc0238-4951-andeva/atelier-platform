package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link PaymentMethod} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(PaymentMethod attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return PaymentMethod.valueOf(dbData.trim());
    }
}
