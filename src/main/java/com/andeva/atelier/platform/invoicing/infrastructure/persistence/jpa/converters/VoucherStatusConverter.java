package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link VoucherStatus} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class VoucherStatusConverter implements AttributeConverter<VoucherStatus, String> {

    @Override
    public String convertToDatabaseColumn(VoucherStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public VoucherStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return VoucherStatus.valueOf(dbData.trim());
    }
}
