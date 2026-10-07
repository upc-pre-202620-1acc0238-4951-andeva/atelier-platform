package com.andeva.atelier.platform.invoicing.infrastructure;

import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.CreditNoteReasonConverter;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.PaymentMethodConverter;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.PaymentStatusConverter;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.VoucherItemTypeConverter;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.VoucherStatusConverter;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.converters.VoucherTypeConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests verifying round-trip conversion and null-safety for Invoicing JPA Attribute Converters.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invoicing JPA Attribute Converters Unit Tests")
class InvoicingJpaConvertersTest {

    private final VoucherTypeConverter voucherTypeConverter = new VoucherTypeConverter();
    private final VoucherStatusConverter voucherStatusConverter = new VoucherStatusConverter();
    private final PaymentMethodConverter paymentMethodConverter = new PaymentMethodConverter();
    private final PaymentStatusConverter paymentStatusConverter = new PaymentStatusConverter();
    private final VoucherItemTypeConverter itemTypeConverter = new VoucherItemTypeConverter();
    private final CreditNoteReasonConverter creditNoteReasonConverter = new CreditNoteReasonConverter();

    @Test
    @DisplayName("Should convert VoucherType correctly")
    void testVoucherTypeConverter() {
        assertThat(voucherTypeConverter.convertToDatabaseColumn(VoucherType.FACTURA)).isEqualTo("FACTURA");
        assertThat(voucherTypeConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(voucherTypeConverter.convertToEntityAttribute("FACTURA")).isEqualTo(VoucherType.FACTURA);
        assertThat(voucherTypeConverter.convertToEntityAttribute("01")).isEqualTo(VoucherType.FACTURA);
        assertThat(voucherTypeConverter.convertToEntityAttribute("03")).isEqualTo(VoucherType.BOLETA);
        assertThat(voucherTypeConverter.convertToEntityAttribute("07")).isEqualTo(VoucherType.NOTA_CREDITO);
        assertThat(voucherTypeConverter.convertToEntityAttribute(null)).isNull();
        assertThat(voucherTypeConverter.convertToEntityAttribute("")).isNull();

        assertThatThrownBy(() -> voucherTypeConverter.convertToEntityAttribute("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should convert VoucherStatus correctly")
    void testVoucherStatusConverter() {
        assertThat(voucherStatusConverter.convertToDatabaseColumn(VoucherStatus.ACCEPTED_SUNAT)).isEqualTo("ACCEPTED_SUNAT");
        assertThat(voucherStatusConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(voucherStatusConverter.convertToEntityAttribute("ACCEPTED_SUNAT")).isEqualTo(VoucherStatus.ACCEPTED_SUNAT);
        assertThat(voucherStatusConverter.convertToEntityAttribute(null)).isNull();
        assertThat(voucherStatusConverter.convertToEntityAttribute("   ")).isNull();

        assertThatThrownBy(() -> voucherStatusConverter.convertToEntityAttribute("INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should convert PaymentMethod correctly")
    void testPaymentMethodConverter() {
        assertThat(paymentMethodConverter.convertToDatabaseColumn(PaymentMethod.DIGITAL_WALLET_YAPE)).isEqualTo("DIGITAL_WALLET_YAPE");
        assertThat(paymentMethodConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(paymentMethodConverter.convertToEntityAttribute("DIGITAL_WALLET_YAPE")).isEqualTo(PaymentMethod.DIGITAL_WALLET_YAPE);
        assertThat(paymentMethodConverter.convertToEntityAttribute(null)).isNull();
        assertThat(paymentMethodConverter.convertToEntityAttribute("   ")).isNull();

        assertThatThrownBy(() -> paymentMethodConverter.convertToEntityAttribute("INVALID_METHOD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should convert PaymentStatus correctly")
    void testPaymentStatusConverter() {
        assertThat(paymentStatusConverter.convertToDatabaseColumn(PaymentStatus.COMPLETED)).isEqualTo("COMPLETED");
        assertThat(paymentStatusConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(paymentStatusConverter.convertToEntityAttribute("COMPLETED")).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(paymentStatusConverter.convertToEntityAttribute(null)).isNull();
        assertThat(paymentStatusConverter.convertToEntityAttribute("   ")).isNull();

        assertThatThrownBy(() -> paymentStatusConverter.convertToEntityAttribute("INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should convert VoucherItemType correctly")
    void testVoucherItemTypeConverter() {
        assertThat(itemTypeConverter.convertToDatabaseColumn(VoucherItemType.PRODUCT)).isEqualTo("PRODUCT");
        assertThat(itemTypeConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(itemTypeConverter.convertToEntityAttribute("SERVICE")).isEqualTo(VoucherItemType.SERVICE);
        assertThat(itemTypeConverter.convertToEntityAttribute(null)).isNull();
        assertThat(itemTypeConverter.convertToEntityAttribute("   ")).isNull();

        assertThatThrownBy(() -> itemTypeConverter.convertToEntityAttribute("INVALID_TYPE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should convert CreditNoteReason correctly")
    void testCreditNoteReasonConverter() {
        assertThat(creditNoteReasonConverter.convertToDatabaseColumn(CreditNoteReason.ANULACION_DE_LA_OPERACION))
                .isEqualTo("ANULACION_DE_LA_OPERACION");
        assertThat(creditNoteReasonConverter.convertToDatabaseColumn(null)).isNull();

        assertThat(creditNoteReasonConverter.convertToEntityAttribute("ANULACION_DE_LA_OPERACION"))
                .isEqualTo(CreditNoteReason.ANULACION_DE_LA_OPERACION);
        assertThat(creditNoteReasonConverter.convertToEntityAttribute(null)).isNull();
        assertThat(creditNoteReasonConverter.convertToEntityAttribute("   ")).isNull();

        assertThatThrownBy(() -> creditNoteReasonConverter.convertToEntityAttribute("INVALID_REASON"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
