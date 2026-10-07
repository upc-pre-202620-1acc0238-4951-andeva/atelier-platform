package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CustomerFiscalDataMissingException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidTaxIdException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.services.VoucherValidationService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for VoucherValidationService domain service.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("VoucherValidationService Domain Service Tests")
class VoucherValidationServiceTest {

    private VoucherValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new VoucherValidationService();
    }

    @Test
    @DisplayName("Should validate valid Modulo 11 Peruvian RUC numbers")
    void shouldValidateValidRucNumbers() {
        // SUNAT RUC: 20100070970
        assertThat(validationService.isValidRuc("20100070970")).isTrue();
        // Valid generated RUC: 20601234565
        assertThat(validationService.isValidRuc("20601234565")).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid RUC formats and incorrect check digits")
    void shouldRejectInvalidRucNumbers() {
        assertThat(validationService.isValidRuc(null)).isFalse();
        assertThat(validationService.isValidRuc("")).isFalse();
        assertThat(validationService.isValidRuc("123")).isFalse();
        // Invalid prefix (starts with 30)
        assertThat(validationService.isValidRuc("30123456789")).isFalse();
        // Invalid check digit for 2010007097
        assertThat(validationService.isValidRuc("20100070979")).isFalse();
    }

    @Test
    @DisplayName("Should accept valid customer fiscal data for Factura")
    void shouldAcceptValidFacturaFiscalData() {
        CustomerFiscalInfo validInfo = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "SUPERINTENDENCIA NACIONAL DE ADUANAS Y DE ADMINISTRACION TRIBUTARIA",
                "AV. GARCILASO DE LA VEGA 1472, LIMA",
                DocumentType.RUC
        );
        Money total = Money.of(new BigDecimal("1500.00"), Currency.PEN);

        assertThatCode(() -> validationService.validateFiscalData(VoucherType.FACTURA, validInfo, total))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw CustomerFiscalDataMissingException when Factura lacks RUC or address or legal name")
    void shouldThrowWhenFacturaMissingRequiredFiscalData() {
        Money total = Money.of(new BigDecimal("1000.00"), Currency.PEN);

        // Missing RUC (using DNI)
        CustomerFiscalInfo dniInfo = CustomerFiscalInfo.of(
                TaxId.dni("12345678"),
                "John Doe",
                "Calle 123",
                DocumentType.DNI
        );
        assertThatThrownBy(() -> validationService.validateFiscalData(VoucherType.FACTURA, dniInfo, total))
                .isInstanceOf(CustomerFiscalDataMissingException.class);

        // Mocked invalid RUC to verify InvalidTaxIdException
        TaxId badTaxId = Mockito.mock(TaxId.class);
        Mockito.when(badTaxId.type()).thenReturn(TaxIdType.RUC);
        Mockito.when(badTaxId.value()).thenReturn("20100070979");

        CustomerFiscalInfo badRucInfo = CustomerFiscalInfo.of(
                badTaxId,
                "Empresa Falsa SAC",
                "Av Los Pinos 100",
                DocumentType.RUC
        );
        assertThatThrownBy(() -> validationService.validateFiscalData(VoucherType.FACTURA, badRucInfo, total))
                .isInstanceOf(InvalidTaxIdException.class);

        // Blank legal name
        CustomerFiscalInfo blankNameInfo = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "   ",
                "Av Los Pinos 100",
                DocumentType.RUC
        );
        assertThatThrownBy(() -> validationService.validateFiscalData(VoucherType.FACTURA, blankNameInfo, total))
                .isInstanceOf(CustomerFiscalDataMissingException.class);

        // Blank fiscal address
        CustomerFiscalInfo blankAddrInfo = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "Empresa Valida SAC",
                "",
                DocumentType.RUC
        );
        assertThatThrownBy(() -> validationService.validateFiscalData(VoucherType.FACTURA, blankAddrInfo, total))
                .isInstanceOf(CustomerFiscalDataMissingException.class);
    }

    @Test
    @DisplayName("Should permit anonymous Boleta de Venta below or equal to S/ 700.00")
    void shouldPermitAnonymousBoletaBelowThreshold() {
        CustomerFiscalInfo anonInfo = CustomerFiscalInfo.anonymous();
        Money total = Money.of(new BigDecimal("700.00"), Currency.PEN);

        assertThatCode(() -> validationService.validateFiscalData(VoucherType.BOLETA, anonInfo, total))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should require customer identification on Boleta exceeding S/ 700.00")
    void shouldRequireIdentificationOnBoletaExceedingThreshold() {
        CustomerFiscalInfo anonInfo = CustomerFiscalInfo.anonymous();
        Money total = Money.of(new BigDecimal("700.01"), Currency.PEN);

        assertThatThrownBy(() -> validationService.validateFiscalData(VoucherType.BOLETA, anonInfo, total))
                .isInstanceOf(CustomerFiscalDataMissingException.class);

        CustomerFiscalInfo identifiedInfo = CustomerFiscalInfo.of(
                TaxId.dni("47895623"),
                "Carlos Mendoza",
                "Av Larco 456",
                DocumentType.DNI
        );

        assertThatCode(() -> validationService.validateFiscalData(VoucherType.BOLETA, identifiedInfo, total))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject Credit Note targeting another Credit Note or a VOIDED voucher")
    void shouldValidateCreditNoteReferenceRules() {
        // Mock Credit Note
        ElectronicVoucher cn = Mockito.mock(ElectronicVoucher.class);
        Mockito.when(cn.getVoucherType()).thenReturn(VoucherType.NOTA_CREDITO);

        assertThatThrownBy(() -> validationService.validateCreditNoteReference(cn, CreditNoteReason.ANULACION_DE_LA_OPERACION))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("cannot reference another Credit Note");

        // Mock Voided voucher
        ElectronicVoucher voided = Mockito.mock(ElectronicVoucher.class);
        Mockito.when(voided.getVoucherType()).thenReturn(VoucherType.FACTURA);
        Mockito.when(voided.getStatus()).thenReturn(VoucherStatus.VOIDED);

        assertThatThrownBy(() -> validationService.validateCreditNoteReference(voided, CreditNoteReason.ANULACION_DE_LA_OPERACION))
                .isInstanceOf(InvoicingDomainException.class)
                .hasMessageContaining("Cannot issue a Credit Note against an already VOIDED");
    }
}
