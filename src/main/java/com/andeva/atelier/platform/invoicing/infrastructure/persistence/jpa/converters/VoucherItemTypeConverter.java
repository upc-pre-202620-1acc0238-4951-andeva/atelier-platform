package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link VoucherItemType} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class VoucherItemTypeConverter implements AttributeConverter<VoucherItemType, String> {

    @Override
    public String convertToDatabaseColumn(VoucherItemType attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public VoucherItemType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return VoucherItemType.valueOf(dbData.trim());
    }
}
