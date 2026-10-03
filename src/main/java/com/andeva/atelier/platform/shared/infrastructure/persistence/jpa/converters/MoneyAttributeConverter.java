package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/**
 * JPA attribute converter for bidirectional and null-safe mapping between the
 * {@link Money} Value Object and a relational {@code NUMERIC(12, 2)} column in PostgreSQL,
 * reconstituting the default platform currency (PEN).
 *
 * @author Joel Huamani Estefanero
 */
@Converter(autoApply = false)
public class MoneyAttributeConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.amount();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal dbData) {
        if (dbData == null) {
            return null;
        }
        return Money.of(dbData, Currency.PEN);
    }
}
