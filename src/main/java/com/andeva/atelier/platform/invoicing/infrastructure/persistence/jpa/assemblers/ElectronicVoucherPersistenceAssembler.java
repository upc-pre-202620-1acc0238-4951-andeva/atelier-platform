package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxIdType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CreditNoteReference;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.SunatResponse;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoidedInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherLinePersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Persistence Assembler converting bidirectionally between {@link ElectronicVoucher} aggregate and {@link ElectronicVoucherPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ElectronicVoucherPersistenceAssembler {

    private final VoucherPaymentPersistenceAssembler paymentAssembler;

    public ElectronicVoucherPersistenceAssembler(VoucherPaymentPersistenceAssembler paymentAssembler) {
        this.paymentAssembler = Objects.requireNonNull(paymentAssembler, "Payment assembler cannot be null");
    }

    public ElectronicVoucherPersistenceEntity toEntity(ElectronicVoucher domain) {
        if (domain == null) {
            return null;
        }

        CustomerFiscalInfo fiscal = domain.getCustomerFiscalInfo();
        DigitalReceiptUrls urls = domain.getDigitalReceiptUrls();
        SunatResponse sunat = domain.getSunatResponse().orElse(null);
        VoidedInfo voided = domain.getVoidedInfo().orElse(null);
        CreditNoteReference ref = domain.getCreditNoteReference().orElse(null);

        ElectronicVoucherPersistenceEntity entity = new ElectronicVoucherPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                domain.getBranchId().value(),
                domain.getCustomerId().value(),
                domain.getWorkOrderId().map(WorkOrderId::value).orElse(null),
                domain.getVoucherType().name(),
                domain.getSerie().value(),
                domain.getNumber().value(),
                domain.getTaxCalculation().subtotal().amount(),
                domain.getTaxCalculation().igvAmount().amount(),
                domain.getTaxCalculation().totalAmount().amount(),
                domain.getCurrency().name(),
                domain.getStatus().name(),
                fiscal.taxId() != null ? fiscal.taxId().value() : "-",
                fiscal.legalName(),
                fiscal.fiscalAddress(),
                fiscal.documentType().name(),
                urls != null ? urls.pdfUrl() : null,
                urls != null ? urls.xmlUrl() : null,
                urls != null ? urls.cdrUrl() : null,
                sunat != null ? sunat.digitalSignatureHash() : null,
                sunat != null ? sunat.responseCode() : null,
                sunat != null ? sunat.description() : null,
                voided != null ? voided.reason() : null,
                voided != null ? voided.voidedAt() : null,
                ref != null ? ref.referenceVoucherId().value() : null,
                ref != null ? ref.referenceSerie().value() : null,
                ref != null ? ref.referenceNumber().value() : null,
                ref != null ? ref.reason().name() : null,
                ref != null ? ref.reasonDescription() : null
        );

        List<VoucherLinePersistenceEntity> lineEntities = new ArrayList<>();
        if (domain.getLines() != null) {
            for (VoucherLine line : domain.getLines()) {
                VoucherLinePersistenceEntity lineEntity = new VoucherLinePersistenceEntity(
                        line.getId().value(),
                        entity,
                        line.getItemId().orElse(null),
                        line.getItemType().name(),
                        line.getDescription(),
                        line.getQuantity().value(),
                        line.getUnitValue().amount(),
                        line.getUnitPrice().amount(),
                        line.getIgvAmount().amount(),
                        line.getTotalLine().amount()
                );
                lineEntities.add(lineEntity);
            }
        }
        entity.setLines(lineEntities);

        List<VoucherPaymentPersistenceEntity> paymentEntities = new ArrayList<>();
        if (domain.getPayments() != null) {
            for (VoucherPayment payment : domain.getPayments()) {
                paymentEntities.add(paymentAssembler.toEntity(payment, entity));
            }
        }
        entity.setPayments(paymentEntities);

        return entity;
    }

    public ElectronicVoucher toDomain(ElectronicVoucherPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        Currency currency = Currency.valueOf(entity.getCurrency() != null ? entity.getCurrency() : "PEN");
        TaxCalculation taxCalc = TaxCalculation.of(
                Money.of(entity.getSubtotal(), currency),
                Money.of(entity.getIgvAmount(), currency),
                Money.of(entity.getTotalAmount(), currency)
        );

        TaxId taxId = null;
        if (entity.getCustomerTaxId() != null && !"-".equals(entity.getCustomerTaxId()) && !entity.getCustomerTaxId().isBlank()) {
            TaxIdType idType = "RUC".equalsIgnoreCase(entity.getCustomerDocumentType()) ? TaxIdType.RUC : TaxIdType.DNI;
            taxId = new TaxId(entity.getCustomerTaxId(), idType);
        }

        DocumentType docType = DocumentType.valueOf(entity.getCustomerDocumentType() != null ? entity.getCustomerDocumentType() : "OTROS");
        CustomerFiscalInfo customerInfo = CustomerFiscalInfo.of(
                taxId,
                entity.getCustomerLegalName(),
                entity.getCustomerFiscalAddress(),
                docType
        );

        DigitalReceiptUrls urls = DigitalReceiptUrls.of(
                entity.getSunatPdfUrl(),
                entity.getSunatXmlUrl(),
                entity.getSunatCdrUrl()
        );

        SunatResponse sunatResponse = null;
        if (entity.getSunatResponseCode() != null || entity.getDigitalSignatureHash() != null) {
            sunatResponse = SunatResponse.of(
                    entity.getSunatResponseCode(),
                    entity.getSunatDescription(),
                    entity.getDigitalSignatureHash()
            );
        }

        VoidedInfo voidedInfo = null;
        if (entity.getVoidedReason() != null && entity.getVoidedAt() != null) {
            voidedInfo = VoidedInfo.of(entity.getVoidedReason(), entity.getVoidedAt());
        }

        CreditNoteReference cnRef = null;
        if (entity.getRefVoucherId() != null && entity.getRefSerie() != null && entity.getRefNumber() != null) {
            CreditNoteReason reason = entity.getRefReason() != null ? CreditNoteReason.valueOf(entity.getRefReason()) : CreditNoteReason.ANULACION_DE_LA_OPERACION;
            cnRef = CreditNoteReference.of(
                    VoucherId.of(entity.getRefVoucherId()),
                    VoucherSerie.of(entity.getRefSerie()),
                    VoucherNumber.of(entity.getRefNumber()),
                    reason,
                    entity.getRefReasonDescription()
            );
        }

        VoucherId voucherId = VoucherId.of(entity.getId());
        List<VoucherLine> lines = new ArrayList<>();
        if (entity.getLines() != null) {
            for (VoucherLinePersistenceEntity lineEntity : entity.getLines()) {
                VoucherLine line = new VoucherLine(
                        VoucherLineId.of(lineEntity.getId()),
                        voucherId,
                        lineEntity.getItemId(),
                        VoucherItemType.valueOf(lineEntity.getItemType()),
                        lineEntity.getDescription(),
                        Quantity.of(lineEntity.getQuantity(), MeasurementUnit.UNIT),
                        Money.of(lineEntity.getUnitValue(), currency),
                        Money.of(lineEntity.getUnitPrice(), currency),
                        Money.of(lineEntity.getIgvAmount(), currency),
                        Money.of(lineEntity.getTotalLine(), currency)
                );
                lines.add(line);
            }
        }

        List<VoucherPayment> payments = new ArrayList<>();
        if (entity.getPayments() != null) {
            for (VoucherPaymentPersistenceEntity paymentEntity : entity.getPayments()) {
                payments.add(paymentAssembler.toDomain(paymentEntity));
            }
        }

        return new ElectronicVoucher(
                voucherId,
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                CustomerId.of(entity.getCustomerId()),
                entity.getWorkOrderId() != null ? WorkOrderId.of(entity.getWorkOrderId()) : null,
                VoucherType.valueOf(entity.getVoucherType()),
                VoucherSerie.of(entity.getSerie()),
                VoucherNumber.of(entity.getNumber()),
                taxCalc,
                currency,
                VoucherStatus.valueOf(entity.getStatus()),
                customerInfo,
                urls,
                sunatResponse,
                voidedInfo,
                cnRef,
                lines,
                payments,
                entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.Instant.now()
        );
    }
}
