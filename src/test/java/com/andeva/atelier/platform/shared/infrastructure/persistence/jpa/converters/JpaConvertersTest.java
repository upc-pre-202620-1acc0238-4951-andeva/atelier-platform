package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.converters;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for JPA Value Object attribute converters.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("JPA Attribute Converters Unit Tests")
class JpaConvertersTest {

    @Nested
    @DisplayName("MoneyAttributeConverter Tests")
    class MoneyConverterTests {
        private final MoneyAttributeConverter converter = new MoneyAttributeConverter();

        @Test
        @DisplayName("Should convert Money to BigDecimal database column")
        void shouldConvertToDatabaseColumn() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();

            Money money = Money.of(new BigDecimal("150.50"), Currency.PEN);
            BigDecimal column = converter.convertToDatabaseColumn(money);

            assertThat(column).isEqualByComparingTo("150.50");
        }

        @Test
        @DisplayName("Should convert BigDecimal to Money entity attribute")
        void shouldConvertToEntityAttribute() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();

            Money money = converter.convertToEntityAttribute(new BigDecimal("299.99"));

            assertThat(money).isNotNull();
            assertThat(money.amount()).isEqualByComparingTo("299.99");
            assertThat(money.currency()).isEqualTo(Currency.PEN);
        }
    }

    @Nested
    @DisplayName("MileageAttributeConverter Tests")
    class MileageConverterTests {
        private final MileageAttributeConverter converter = new MileageAttributeConverter();

        @Test
        @DisplayName("Should convert Mileage to Integer database column")
        void shouldConvertToDatabaseColumn() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();

            Mileage mileage = Mileage.of(45000);
            Integer column = converter.convertToDatabaseColumn(mileage);

            assertThat(column).isEqualTo(45000);
        }

        @Test
        @DisplayName("Should convert Integer to Mileage entity attribute")
        void shouldConvertToEntityAttribute() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();

            Mileage mileage = converter.convertToEntityAttribute(82150);

            assertThat(mileage).isNotNull();
            assertThat(mileage.value()).isEqualTo(82150);
        }
    }

    @Nested
    @DisplayName("TaxIdAttributeConverter Tests")
    class TaxIdConverterTests {
        private final TaxIdAttributeConverter converter = new TaxIdAttributeConverter();

        @Test
        @DisplayName("Should convert TaxId to String database column")
        void shouldConvertToDatabaseColumn() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();

            TaxId ruc = TaxId.ruc("20100070970");
            assertThat(converter.convertToDatabaseColumn(ruc)).isEqualTo("20100070970");

            TaxId dni = TaxId.dni("47895623");
            assertThat(converter.convertToDatabaseColumn(dni)).isEqualTo("47895623");
        }

        @Test
        @DisplayName("Should convert database String to TaxId entity attribute deducing type")
        void shouldConvertToEntityAttribute() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
            assertThat(converter.convertToEntityAttribute("   ")).isNull();

            TaxId ruc = converter.convertToEntityAttribute("20100070970");
            assertThat(ruc).isNotNull();
            assertThat(ruc.value()).isEqualTo("20100070970");
            assertThat(ruc.type()).isEqualTo(TaxIdType.RUC);

            TaxId dni = converter.convertToEntityAttribute("47895623");
            assertThat(dni).isNotNull();
            assertThat(dni.value()).isEqualTo("47895623");
            assertThat(dni.type()).isEqualTo(TaxIdType.DNI);
        }
    }

    @Nested
    @DisplayName("EmailAddressAttributeConverter Tests")
    class EmailConverterTests {
        private final EmailAddressAttributeConverter converter = new EmailAddressAttributeConverter();

        @Test
        @DisplayName("Should convert EmailAddress to String database column")
        void shouldConvertToDatabaseColumn() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();

            EmailAddress email = EmailAddress.of("support@atelier.pe");
            assertThat(converter.convertToDatabaseColumn(email)).isEqualTo("support@atelier.pe");
        }

        @Test
        @DisplayName("Should convert String to EmailAddress entity attribute")
        void shouldConvertToEntityAttribute() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
            assertThat(converter.convertToEntityAttribute("   ")).isNull();

            EmailAddress email = converter.convertToEntityAttribute("contact@andeva.pe");
            assertThat(email).isNotNull();
            assertThat(email.value()).isEqualTo("contact@andeva.pe");
        }
    }

    @Nested
    @DisplayName("PhoneNumberAttributeConverter Tests")
    class PhoneConverterTests {
        private final PhoneNumberAttributeConverter converter = new PhoneNumberAttributeConverter();

        @Test
        @DisplayName("Should convert PhoneNumber to String database column")
        void shouldConvertToDatabaseColumn() {
            assertThat(converter.convertToDatabaseColumn(null)).isNull();

            PhoneNumber phone = PhoneNumber.of("+51987654321");
            assertThat(converter.convertToDatabaseColumn(phone)).isEqualTo("+51987654321");
        }

        @Test
        @DisplayName("Should convert String to PhoneNumber entity attribute")
        void shouldConvertToEntityAttribute() {
            assertThat(converter.convertToEntityAttribute(null)).isNull();
            assertThat(converter.convertToEntityAttribute("   ")).isNull();

            PhoneNumber phone = converter.convertToEntityAttribute("+51912345678");
            assertThat(phone).isNotNull();
            assertThat(phone.value()).isEqualTo("+51912345678");
        }
    }
}
