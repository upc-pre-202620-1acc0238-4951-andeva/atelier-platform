package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.hr.domain.model.enums.PayrollStatus;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters.PayrollStatusAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "payroll_payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payroll_membership_period", columnNames = {"membership_id", "period_start", "period_end"})
        },
        indexes = {
                @Index(name = "idx_payroll_tenant_period", columnList = "tenant_id, period_start, period_end"),
                @Index(name = "idx_payroll_membership", columnList = "membership_id"),
                @Index(name = "idx_payroll_status", columnList = "tenant_id, status")
        }
)
public class PayrollPaymentPersistenceEntity extends HrAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Column(name = "membership_id", columnDefinition = "uuid", nullable = false)
    private UUID membershipId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "base_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "deductions", precision = 10, scale = 2, nullable = false)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "bonuses", precision = 10, scale = 2, nullable = false)
    private BigDecimal bonuses = BigDecimal.ZERO;

    @Column(name = "total_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "PEN";

    @Convert(converter = PayrollStatusAttributeConverter.class)
    @Column(name = "status", length = 20, nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @OneToMany(mappedBy = "payrollPayment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayrollItemPersistenceEntity> items = new ArrayList<>();

    public PayrollPaymentPersistenceEntity(UUID id) {
        super(id);
    }
}
