package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Persistence Entity mapping electronic vouchers with SUNAT fiscal validity to the database.
 *
 * @author Joel Huamani Estefanero
 */
@Entity
@Table(name = "electronic_vouchers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vouchers_tenant_serie_number", columnNames = {"tenant_id", "serie", "number"})
}, indexes = {
        @Index(name = "idx_vouchers_tenant_created", columnList = "tenant_id, created_at"),
        @Index(name = "idx_vouchers_work_order", columnList = "work_order_id"),
        @Index(name = "idx_vouchers_customer", columnList = "customer_id")
})
public class ElectronicVoucherPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private UUID branchId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "work_order_id")
    private UUID workOrderId;

    @Column(name = "voucher_type", nullable = false, length = 20)
    private String voucherType;

    @Column(name = "serie", nullable = false, length = 4)
    private String serie;

    @Column(name = "number", nullable = false)
    private int number;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal igvAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "PEN";

    @Column(name = "status", nullable = false, length = 25)
    private String status;

    @Column(name = "customer_tax_id", nullable = false, length = 20)
    private String customerTaxId;

    @Column(name = "customer_legal_name", nullable = false, length = 150)
    private String customerLegalName;

    @Column(name = "customer_fiscal_address", nullable = false, length = 200)
    private String customerFiscalAddress;

    @Column(name = "customer_document_type", nullable = false, length = 15)
    private String customerDocumentType;

    @Column(name = "sunat_pdf_url", length = 255)
    private String sunatPdfUrl;

    @Column(name = "sunat_xml_url", length = 255)
    private String sunatXmlUrl;

    @Column(name = "sunat_cdr_url", length = 255)
    private String sunatCdrUrl;

    @Column(name = "digital_signature_hash", length = 100)
    private String digitalSignatureHash;

    @Column(name = "sunat_response_code", length = 10)
    private String sunatResponseCode;

    @Column(name = "sunat_description", length = 255)
    private String sunatDescription;

    @Column(name = "voided_reason", length = 255)
    private String voidedReason;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "ref_voucher_id")
    private UUID refVoucherId;

    @Column(name = "ref_serie", length = 4)
    private String refSerie;

    @Column(name = "ref_number")
    private Integer refNumber;

    @Column(name = "ref_reason", length = 50)
    private String refReason;

    @Column(name = "ref_reason_description", length = 255)
    private String refReasonDescription;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VoucherLinePersistenceEntity> lines = new ArrayList<>();

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VoucherPaymentPersistenceEntity> payments = new ArrayList<>();

    public ElectronicVoucherPersistenceEntity() {
    }

    public ElectronicVoucherPersistenceEntity(
            UUID id,
            UUID tenantId,
            UUID branchId,
            UUID customerId,
            UUID workOrderId,
            String voucherType,
            String serie,
            int number,
            BigDecimal subtotal,
            BigDecimal igvAmount,
            BigDecimal totalAmount,
            String currency,
            String status,
            String customerTaxId,
            String customerLegalName,
            String customerFiscalAddress,
            String customerDocumentType,
            String sunatPdfUrl,
            String sunatXmlUrl,
            String sunatCdrUrl,
            String digitalSignatureHash,
            String sunatResponseCode,
            String sunatDescription,
            String voidedReason,
            Instant voidedAt,
            UUID refVoucherId,
            String refSerie,
            Integer refNumber,
            String refReason,
            String refReasonDescription
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.branchId = branchId;
        this.customerId = customerId;
        this.workOrderId = workOrderId;
        this.voucherType = voucherType;
        this.serie = serie;
        this.number = number;
        this.subtotal = subtotal;
        this.igvAmount = igvAmount;
        this.totalAmount = totalAmount;
        this.currency = currency != null ? currency : "PEN";
        this.status = status;
        this.customerTaxId = customerTaxId;
        this.customerLegalName = customerLegalName;
        this.customerFiscalAddress = customerFiscalAddress;
        this.customerDocumentType = customerDocumentType;
        this.sunatPdfUrl = sunatPdfUrl;
        this.sunatXmlUrl = sunatXmlUrl;
        this.sunatCdrUrl = sunatCdrUrl;
        this.digitalSignatureHash = digitalSignatureHash;
        this.sunatResponseCode = sunatResponseCode;
        this.sunatDescription = sunatDescription;
        this.voidedReason = voidedReason;
        this.voidedAt = voidedAt;
        this.refVoucherId = refVoucherId;
        this.refSerie = refSerie;
        this.refNumber = refNumber;
        this.refReason = refReason;
        this.refReasonDescription = refReasonDescription;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(UUID workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgvAmount() {
        return igvAmount;
    }

    public void setIgvAmount(BigDecimal igvAmount) {
        this.igvAmount = igvAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerTaxId() {
        return customerTaxId;
    }

    public void setCustomerTaxId(String customerTaxId) {
        this.customerTaxId = customerTaxId;
    }

    public String getCustomerLegalName() {
        return customerLegalName;
    }

    public void setCustomerLegalName(String customerLegalName) {
        this.customerLegalName = customerLegalName;
    }

    public String getCustomerFiscalAddress() {
        return customerFiscalAddress;
    }

    public void setCustomerFiscalAddress(String customerFiscalAddress) {
        this.customerFiscalAddress = customerFiscalAddress;
    }

    public String getCustomerDocumentType() {
        return customerDocumentType;
    }

    public void setCustomerDocumentType(String customerDocumentType) {
        this.customerDocumentType = customerDocumentType;
    }

    public String getSunatPdfUrl() {
        return sunatPdfUrl;
    }

    public void setSunatPdfUrl(String sunatPdfUrl) {
        this.sunatPdfUrl = sunatPdfUrl;
    }

    public String getSunatXmlUrl() {
        return sunatXmlUrl;
    }

    public void setSunatXmlUrl(String sunatXmlUrl) {
        this.sunatXmlUrl = sunatXmlUrl;
    }

    public String getSunatCdrUrl() {
        return sunatCdrUrl;
    }

    public void setSunatCdrUrl(String sunatCdrUrl) {
        this.sunatCdrUrl = sunatCdrUrl;
    }

    public String getDigitalSignatureHash() {
        return digitalSignatureHash;
    }

    public void setDigitalSignatureHash(String digitalSignatureHash) {
        this.digitalSignatureHash = digitalSignatureHash;
    }

    public String getSunatResponseCode() {
        return sunatResponseCode;
    }

    public void setSunatResponseCode(String sunatResponseCode) {
        this.sunatResponseCode = sunatResponseCode;
    }

    public String getSunatDescription() {
        return sunatDescription;
    }

    public void setSunatDescription(String sunatDescription) {
        this.sunatDescription = sunatDescription;
    }

    public String getVoidedReason() {
        return voidedReason;
    }

    public void setVoidedReason(String voidedReason) {
        this.voidedReason = voidedReason;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public void setVoidedAt(Instant voidedAt) {
        this.voidedAt = voidedAt;
    }

    public UUID getRefVoucherId() {
        return refVoucherId;
    }

    public void setRefVoucherId(UUID refVoucherId) {
        this.refVoucherId = refVoucherId;
    }

    public String getRefSerie() {
        return refSerie;
    }

    public void setRefSerie(String refSerie) {
        this.refSerie = refSerie;
    }

    public Integer getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(Integer refNumber) {
        this.refNumber = refNumber;
    }

    public String getRefReason() {
        return refReason;
    }

    public void setRefReason(String refReason) {
        this.refReason = refReason;
    }

    public String getRefReasonDescription() {
        return refReasonDescription;
    }

    public void setRefReasonDescription(String refReasonDescription) {
        this.refReasonDescription = refReasonDescription;
    }

    public List<VoucherLinePersistenceEntity> getLines() {
        return lines;
    }

    public void setLines(List<VoucherLinePersistenceEntity> lines) {
        this.lines = lines;
    }

    public List<VoucherPaymentPersistenceEntity> getPayments() {
        return payments;
    }

    public void setPayments(List<VoucherPaymentPersistenceEntity> payments) {
        this.payments = payments;
    }
}
