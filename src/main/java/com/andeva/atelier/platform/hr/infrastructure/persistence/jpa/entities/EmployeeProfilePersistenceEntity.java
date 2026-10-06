package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters.CompensationTypeAttributeConverter;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.converters.EmploymentStatusAttributeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "employee_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_profiles_membership", columnNames = {"membership_id"})
        },
        indexes = {
                @Index(name = "idx_employee_profiles_branch", columnList = "branch_id"),
                @Index(name = "idx_employee_profiles_membership", columnList = "membership_id"),
                @Index(name = "idx_employee_profiles_tenant_status", columnList = "tenant_id, employment_status")
        }
)
public class EmployeeProfilePersistenceEntity extends HrAuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
    private UUID tenantId;

    @Column(name = "branch_id", columnDefinition = "uuid", nullable = false)
    private UUID branchId;

    @Column(name = "membership_id", columnDefinition = "uuid", nullable = false)
    private UUID membershipId;

    @Column(name = "assigned_shift_id", columnDefinition = "uuid")
    private UUID assignedShiftId;

    @Column(name = "base_salary", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "PEN";

    @Convert(converter = CompensationTypeAttributeConverter.class)
    @Column(name = "salary_type", length = 20, nullable = false)
    private CompensationType compensationType = CompensationType.MONTHLY_FIXED;

    @Column(name = "job_title", length = 100, nullable = false)
    private String jobTitle;

    @Convert(converter = EmploymentStatusAttributeConverter.class)
    @Column(name = "employment_status", length = 20, nullable = false)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    public EmployeeProfilePersistenceEntity(UUID id) {
        super(id);
    }
}
