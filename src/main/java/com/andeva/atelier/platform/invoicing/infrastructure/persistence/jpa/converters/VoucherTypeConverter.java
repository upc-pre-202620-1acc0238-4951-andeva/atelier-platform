package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Attribute Converter for {@link VoucherType} enum.
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = true)
public class VoucherTypeConverter implements AttributeConverter<VoucherType, String> {

    @Override
    public String convertToDatabaseColumn(VoucherType attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public VoucherType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return VoucherType.valueOf(dbData.trim());
        } catch (IllegalArgumentException e) {
            // Support legacy SUNAT codes if needed: 01 -> FACTURA, 03 -> BOLETA, 07 -> NOTA_CREDITO
            return switch (dbData.trim()) {
                case "01" -> VoucherType.FACTURA;
                case "03" -> VoucherType.BOLETA;
                case "07" -> VoucherType.NOTA_CREDITO;
                default -> throw new IllegalArgumentException("Unknown VoucherType code: " + dbData);
            };
        }
    }
}
