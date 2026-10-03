package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.converters.InvoiceStatusConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code invoices} relational table.
 * Persists B2B SaaS revenue receipts and hosted billing invoice URLs issued to workshop tenants.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "saas_invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_saas_invoices_stripe_invoice", columnNames = {"stripe_invoice_id"})
        },
        indexes = {
                @Index(name = "idx_saas_invoices_subscription", columnList = "subscription_id"),
                @Index(name = "idx_saas_invoices_tenant_paid", columnList = "tenant_id, paid_at"),
                @Index(name = "idx_saas_invoices_status", columnList = "status"),
                @Index(name = "idx_saas_invoices_stripe", columnList = "stripe_invoice_id")
        }
)
public class SaasInvoicePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "subscription_id", nullable = false, updatable = false)
    private UUID subscriptionId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "stripe_invoice_id", nullable = false, length = 100, unique = true)
    private String stripeInvoiceId;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Convert(converter = InvoiceStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "invoice_pdf_url", length = 255)
    private String invoicePdfUrl;

    @Column(name = "hosted_invoice_url", length = 255)
    private String hostedInvoiceUrl;

    @Column(name = "paid_at")
    private Instant paidAt;

    public SaasInvoicePersistenceEntity(UUID id) {
        super(id);
    }
}
