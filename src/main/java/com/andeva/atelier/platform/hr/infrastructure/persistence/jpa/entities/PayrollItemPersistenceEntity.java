package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.hr.domain.model.enums.PayrollItemCategory;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters.PayrollItemCategoryAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "payroll_items",
        indexes = {
                @Index(name = "idx_payroll_items_payment", columnList = "payroll_payment_id"),
                @Index(name = "idx_payroll_items_category", columnList = "category")
        }
)
public class PayrollItemPersistenceEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_payment_id", nullable = false, updatable = false)
    private PayrollPaymentPersistenceEntity payrollPayment;

    @Convert(converter = PayrollItemCategoryAttributeConverter.class)
    @Column(name = "category", length = 20, nullable = false)
    private PayrollItemCategory category;

    @Column(name = "concept", length = 200, nullable = false)
    private String concept;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PayrollItemPersistenceEntity(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayrollItemPersistenceEntity that = (PayrollItemPersistenceEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
