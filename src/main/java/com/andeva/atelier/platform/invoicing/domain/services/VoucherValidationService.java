package com.andeva.atelier.platform.invoicing.domain.services;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CustomerFiscalDataMissingException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidTaxIdException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Domain service enforcing Peruvian tax compliance rules and fiscal validation:
 * - Algorithmic Modulo 11 check-digit verification for RUCs.
 * - Minimum identification threshold (S/ 700.00) for Boletas de Venta.
 * - Mandatory fiscal address and legal company name for Facturas.
 * - Integrity and referential consistency for Credit Notes.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class VoucherValidationService {

    private static final Pattern RUC_PATTERN = Pattern.compile("^(10|15|17|20)\\d{9}$");
    private static final int[] RUC_WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
    public static final BigDecimal ANONYMOUS_BOLETA_THRESHOLD = BigDecimal.valueOf(700.00);

    public boolean isValidRuc(String ruc) {
        if (ruc == null) return false;
        String trimmed = ruc.trim();
        if (!RUC_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(trimmed.charAt(i)) * RUC_WEIGHTS[i];
        }
        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10) checkDigit = 0;
        else if (checkDigit == 11) checkDigit = 1;

        return checkDigit == Character.getNumericValue(trimmed.charAt(10));
    }

    public void validateFiscalData(VoucherType type, CustomerFiscalInfo info, Money totalAmount) {
        Objects.requireNonNull(type, "Voucher type cannot be null");
        Objects.requireNonNull(info, "Customer fiscal info cannot be null");
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");

        if (type == VoucherType.FACTURA) {
            if (info.taxId() == null || info.taxId().type() != TaxIdType.RUC) {
                throw new CustomerFiscalDataMissingException("Factura requires a valid customer RUC tax identification");
            }
            if (!isValidRuc(info.taxId().value())) {
                throw new InvalidTaxIdException(info.taxId().value());
            }
            if (info.legalName().isBlank()) {
                throw new CustomerFiscalDataMissingException("Factura requires customer legal company name (razón social)");
            }
            if (info.fiscalAddress().isBlank()) {
                throw new CustomerFiscalDataMissingException("Factura requires customer fiscal address");
            }
        } else if (type == VoucherType.BOLETA) {
            if (totalAmount.amount().compareTo(ANONYMOUS_BOLETA_THRESHOLD) > 0) {
                if (info.taxId() == null || info.legalName().isBlank()) {
                    throw new CustomerFiscalDataMissingException(
                            "Boleta de Venta exceeding S/ " + ANONYMOUS_BOLETA_THRESHOLD +
                                    " requires customer identity document (DNI/CE/RUC) and full name"
                    );
                }
            }
        }
    }

    public void validateCreditNoteReference(ElectronicVoucher originalVoucher, CreditNoteReason reason) {
        Objects.requireNonNull(originalVoucher, "Original voucher cannot be null");
        Objects.requireNonNull(reason, "Credit note reason cannot be null");

        if (originalVoucher.getVoucherType() == VoucherType.NOTA_CREDITO) {
            throw new InvoicingDomainException(
                    "ERR_INVALID_CREDIT_NOTE_TARGET",
                    "A Credit Note cannot reference another Credit Note"
            );
        }

        if (originalVoucher.getStatus() == VoucherStatus.VOIDED) {
            throw new InvoicingDomainException(
                    "ERR_ORIGINAL_VOUCHER_VOIDED",
                    "Cannot issue a Credit Note against an already VOIDED electronic voucher"
            );
        }

        if (originalVoucher.getStatus() != VoucherStatus.ACCEPTED_SUNAT) {
            throw new InvoicingDomainException(
                    "ERR_ORIGINAL_VOUCHER_NOT_ACCEPTED",
                    "A Credit Note can only be issued against an electronic voucher accepted by SUNAT"
            );
        }
    }
}
