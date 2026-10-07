package com.andeva.atelier.platform.invoicing.domain.model.aggregates;

import com.andeva.atelier.platform.invoicing.domain.exceptions.CustomerFiscalDataMissingException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherAlreadyPaidException;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherImmutableException;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.events.CreditNoteIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.ElectronicVoucherIssuedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherPaymentRegisteredEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherRejectedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherVoidedEvent;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.SunatResponse;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoidedInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root governing the legal, financial, and regulatory lifecycle of an electronic fiscal voucher
 * (Factura, Boleta de Venta, Nota de Crédito) under SUNAT UBL 2.1 regulations.
 *
 * @author Joel Huamani Estefanero
 */
public class ElectronicVoucher extends AbstractDomainAggregateRoot<ElectronicVoucher> {

    public static final BigDecimal ANONYMOUS_BOLETA_THRESHOLD = BigDecimal.valueOf(700.00);

    private final VoucherId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private final CustomerId customerId;
    private final WorkOrderId workOrderId;
    private final VoucherType voucherType;
    private final VoucherSerie serie;
    private final VoucherNumber number;
    private final TaxCalculation taxCalculation;
    private final Currency currency;
    private VoucherStatus status;
    private final CustomerFiscalInfo customerFiscalInfo;
    private DigitalReceiptUrls digitalReceiptUrls;
    private SunatResponse sunatResponse;
    private VoidedInfo voidedInfo;
    private final CreditNoteReference creditNoteReference;
    private final List<VoucherLine> lines;
    private final List<VoucherPayment> payments;
    private final Instant issuedAt;

    public ElectronicVoucher(
            VoucherId id,
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            WorkOrderId workOrderId,
            VoucherType voucherType,
            VoucherSerie serie,
            VoucherNumber number,
            TaxCalculation taxCalculation,
            Currency currency,
            VoucherStatus status,
            CustomerFiscalInfo customerFiscalInfo,
            DigitalReceiptUrls digitalReceiptUrls,
            SunatResponse sunatResponse,
            VoidedInfo voidedInfo,
            CreditNoteReference creditNoteReference,
            List<VoucherLine> lines,
            List<VoucherPayment> payments,
            Instant issuedAt
    ) {
        this.id = Objects.requireNonNull(id, "Voucher ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "Branch ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.workOrderId = workOrderId;
        this.voucherType = Objects.requireNonNull(voucherType, "Voucher type cannot be null");
        this.serie = Objects.requireNonNull(serie, "Voucher series cannot be null");
        this.number = Objects.requireNonNull(number, "Voucher number cannot be null");
        this.taxCalculation = Objects.requireNonNull(taxCalculation, "Tax calculation cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");
        this.status = Objects.requireNonNull(status, "Voucher status cannot be null");
        this.customerFiscalInfo = Objects.requireNonNull(customerFiscalInfo, "Customer fiscal info cannot be null");
        this.digitalReceiptUrls = digitalReceiptUrls != null ? digitalReceiptUrls : DigitalReceiptUrls.empty();
        this.sunatResponse = sunatResponse;
        this.voidedInfo = voidedInfo;
        this.creditNoteReference = creditNoteReference;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.payments = payments != null ? new ArrayList<>(payments) : new ArrayList<>();
        this.issuedAt = issuedAt != null ? issuedAt : Instant.now();

        validateInvariants();
    }

    public static ElectronicVoucher issue(
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            WorkOrderId workOrderId,
            VoucherType type,
            VoucherSerie serie,
            VoucherNumber number,
            CustomerFiscalInfo customerInfo,
            Currency currency,
            TaxCalculation taxCalculation,
            List<VoucherLine> lines
    ) {
        if (type == VoucherType.NOTA_CREDITO) {
            throw new IllegalArgumentException("Use issueCreditNote(...) to issue credit notes");
        }

        VoucherId voucherId = VoucherId.generate();
        ElectronicVoucher voucher = new ElectronicVoucher(
                voucherId,
                tenantId,
                branchId,
                customerId,
                workOrderId,
                type,
                serie,
                number,
                taxCalculation,
                currency,
                VoucherStatus.ISSUED,
                customerInfo,
                DigitalReceiptUrls.empty(),
                null,
                null,
                null,
                lines,
                new ArrayList<>(),
                Instant.now()
        );

        voucher.registerEvent(ElectronicVoucherIssuedEvent.of(
                voucherId,
                tenantId,
                branchId,
                customerId,
                workOrderId,
                type,
                serie,
                number,
                taxCalculation.totalAmount(),
                currency
        ));

        return voucher;
    }

    public static ElectronicVoucher issueCreditNote(
            TenantId tenantId,
            BranchId branchId,
            CustomerId customerId,
            WorkOrderId workOrderId,
            VoucherSerie serie,
            VoucherNumber number,
            CustomerFiscalInfo customerInfo,
            Currency currency,
            TaxCalculation taxCalculation,
            CreditNoteReference creditNoteReference,
            List<VoucherLine> lines
    ) {
        Objects.requireNonNull(creditNoteReference, "Credit note reference cannot be null");

        VoucherId creditNoteId = VoucherId.generate();
        ElectronicVoucher creditNote = new ElectronicVoucher(
                creditNoteId,
                tenantId,
                branchId,
                customerId,
                workOrderId,
                VoucherType.NOTA_CREDITO,
                serie,
                number,
                taxCalculation,
                currency,
                VoucherStatus.ISSUED,
                customerInfo,
                DigitalReceiptUrls.empty(),
                null,
                null,
                creditNoteReference,
                lines,
                new ArrayList<>(),
                Instant.now()
        );

        creditNote.registerEvent(CreditNoteIssuedEvent.of(
                creditNoteId,
                creditNoteReference.referenceVoucherId(),
                tenantId,
                branchId,
                serie,
                number,
                creditNoteReference.reason(),
                taxCalculation.totalAmount()
        ));

        return creditNote;
    }

    private void validateInvariants() {
        if (lines.isEmpty()) {
            throw new InvalidVoucherAmountException("Electronic voucher must contain at least one billable line item");
        }

        // Validate line total sum arithmetic
        BigDecimal lineSum = lines.stream()
                .map(l -> l.getTotalLine().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lineSum.compareTo(taxCalculation.totalAmount().amount()) != 0) {
            throw new InvalidVoucherAmountException(
                    "Sum of voucher line totals (" + lineSum + ") does not equal header total amount (" +
                            taxCalculation.totalAmount().amount() + ")"
            );
        }

        // Fiscal validations
        if (voucherType == VoucherType.FACTURA) {
            if (customerFiscalInfo.taxId() == null || customerFiscalInfo.taxId().type() != TaxIdType.RUC) {
                throw new CustomerFiscalDataMissingException("Factura requires a valid customer RUC tax identification");
            }
            if (customerFiscalInfo.legalName().isBlank()) {
                throw new CustomerFiscalDataMissingException("Factura requires non-blank customer legal company name (razón social)");
            }
            if (customerFiscalInfo.fiscalAddress().isBlank()) {
                throw new CustomerFiscalDataMissingException("Factura requires non-blank customer fiscal address");
            }
        } else if (voucherType == VoucherType.BOLETA) {
            if (taxCalculation.totalAmount().amount().compareTo(ANONYMOUS_BOLETA_THRESHOLD) > 0) {
                if (customerFiscalInfo.taxId() == null || customerFiscalInfo.legalName().isBlank()) {
                    throw new CustomerFiscalDataMissingException(
                            "Boleta exceeding S/ " + ANONYMOUS_BOLETA_THRESHOLD + " requires customer tax identification and full name per SUNAT regulations"
                    );
                }
            }
        } else if (voucherType == VoucherType.NOTA_CREDITO) {
            if (creditNoteReference == null) {
                throw new IllegalArgumentException("Credit note must reference an original electronic voucher");
            }
        }
    }

    public void markAcceptedBySunat(String digitalSignatureHash, String description, DigitalReceiptUrls urls) {
        if (this.status == VoucherStatus.VOIDED) {
            throw new VoucherImmutableException("Cannot mark a VOIDED voucher as ACCEPTED_SUNAT");
        }

        this.status = VoucherStatus.ACCEPTED_SUNAT;
        this.sunatResponse = SunatResponse.of("0", description, digitalSignatureHash);
        if (urls != null) {
            this.digitalReceiptUrls = urls;
        }

        registerEvent(VoucherAcceptedBySunatEvent.of(
                id,
                tenantId,
                digitalSignatureHash,
                this.digitalReceiptUrls
        ));
    }

    public void markRejectedBySunat(String errorCode, String errorMessage) {
        if (this.status == VoucherStatus.ACCEPTED_SUNAT) {
            throw new VoucherImmutableException(id);
        }

        this.status = VoucherStatus.REJECTED_SUNAT;
        this.sunatResponse = SunatResponse.of(errorCode, errorMessage, "");

        registerEvent(VoucherRejectedBySunatEvent.of(
                id,
                tenantId,
                errorCode,
                errorMessage
        ));
    }

    public void voidVoucher(String reason, Instant voidTimestamp) {
        if (this.status == VoucherStatus.VOIDED) {
            return; // Idempotent
        }

        this.status = VoucherStatus.VOIDED;
        this.voidedInfo = VoidedInfo.of(reason, voidTimestamp != null ? voidTimestamp : Instant.now());

        registerEvent(VoucherVoidedEvent.of(
                id,
                tenantId,
                reason
        ));
    }

    public VoucherPayment recordPayment(
            PaymentId paymentId,
            Money amount,
            PaymentMethod method,
            String transactionRef
    ) {
        if (this.status == VoucherStatus.VOIDED || this.status == VoucherStatus.REJECTED_SUNAT) {
            throw new VoucherImmutableException("Cannot record payment on a " + this.status + " voucher: " + id.value());
        }

        if (isFullyPaid()) {
            throw new VoucherAlreadyPaidException(id);
        }

        Money pending = getPendingBalance();
        if (amount.amount().compareTo(pending.amount()) > 0) {
            throw new VoucherAlreadyPaidException(
                    "Payment amount " + amount.amount() + " exceeds remaining balance " + pending.amount()
            );
        }

        VoucherPayment payment = VoucherPayment.record(
                paymentId,
                id,
                tenantId,
                branchId,
                amount,
                method,
                transactionRef
        );

        this.payments.add(payment);

        registerEvent(VoucherPaymentRegisteredEvent.of(
                payment.getId(),
                id,
                tenantId,
                branchId,
                amount,
                method,
                isFullyPaid()
        ));

        return payment;
    }

    public boolean isFullyPaid() {
        return getPendingBalance().amount().compareTo(BigDecimal.ZERO) <= 0;
    }

    public Money getTotalAmount() {
        return taxCalculation.totalAmount();
    }

    public Money getPendingBalance() {
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(p -> p.getAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = taxCalculation.totalAmount().amount().subtract(totalPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        return Money.of(remaining, currency);
    }

    public VoucherId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public BranchId getBranchId() {
        return branchId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Optional<WorkOrderId> getWorkOrderId() {
        return Optional.ofNullable(workOrderId);
    }

    public VoucherType getVoucherType() {
        return voucherType;
    }

    public VoucherSerie getSerie() {
        return serie;
    }

    public VoucherNumber getNumber() {
        return number;
    }

    public TaxCalculation getTaxCalculation() {
        return taxCalculation;
    }

    public Currency getCurrency() {
        return currency;
    }

    public VoucherStatus getStatus() {
        return status;
    }

    public CustomerFiscalInfo getCustomerFiscalInfo() {
        return customerFiscalInfo;
    }

    public DigitalReceiptUrls getDigitalReceiptUrls() {
        return digitalReceiptUrls;
    }

    public Optional<SunatResponse> getSunatResponse() {
        return Optional.ofNullable(sunatResponse);
    }

    public Optional<VoidedInfo> getVoidedInfo() {
        return Optional.ofNullable(voidedInfo);
    }

    public Optional<CreditNoteReference> getCreditNoteReference() {
        return Optional.ofNullable(creditNoteReference);
    }

    public List<VoucherLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public List<VoucherPayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
