package com.andeva.atelier.platform.hr.domain.model.aggregates;

import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.domain.model.events.EmployeeProfileRegisteredEvent;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public class EmployeeProfile extends AbstractDomainAggregateRoot<EmployeeProfile> {

    private final EmployeeProfileId id;
    private final TenantId tenantId;
    private BranchId branchId;
    private final TenantMembershipId membershipId;
    private WorkShiftId assignedShiftId;
    private Money baseSalary;
    private CompensationType compensationType;
    private String jobTitle;
    private EmploymentStatus employmentStatus;

    public EmployeeProfile(
            EmployeeProfileId id,
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            WorkShiftId assignedShiftId,
            Money baseSalary,
            CompensationType compensationType,
            String jobTitle,
            EmploymentStatus employmentStatus
    ) {
        this.id = Objects.requireNonNull(id, "EmployeeProfileId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "BranchId cannot be null");
        this.membershipId = Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        this.assignedShiftId = assignedShiftId;
        this.baseSalary = Objects.requireNonNull(baseSalary, "baseSalary cannot be null");
        this.compensationType = compensationType != null ? compensationType : CompensationType.MONTHLY_FIXED;
        this.jobTitle = Objects.requireNonNull(jobTitle, "jobTitle cannot be null");
        this.employmentStatus = employmentStatus != null ? employmentStatus : EmploymentStatus.ACTIVE;
    }

    public static EmployeeProfile register(
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            WorkShiftId assignedShiftId,
            Money baseSalary,
            CompensationType compensationType,
            String jobTitle
    ) {
        EmployeeProfileId profileId = EmployeeProfileId.generate();
        EmployeeProfile profile = new EmployeeProfile(
                profileId, tenantId, branchId, membershipId, assignedShiftId,
                baseSalary, compensationType, jobTitle, EmploymentStatus.ACTIVE
        );
        profile.registerEvent(EmployeeProfileRegisteredEvent.now(profileId, tenantId, branchId, membershipId));
        return profile;
    }

    public void assignShift(WorkShiftId newShiftId) {
        this.assignedShiftId = Objects.requireNonNull(newShiftId, "WorkShiftId cannot be null");
    }

    public void updateSalary(Money newSalary, CompensationType newType) {
        this.baseSalary = Objects.requireNonNull(newSalary, "newSalary cannot be null");
        if (newType != null) {
            this.compensationType = newType;
        }
    }

    public void changeBranch(BranchId newBranchId) {
        this.branchId = Objects.requireNonNull(newBranchId, "BranchId cannot be null");
    }

    public void updateStatus(EmploymentStatus newStatus) {
        this.employmentStatus = Objects.requireNonNull(newStatus, "EmploymentStatus cannot be null");
    }

    public boolean isActive() {
        return this.employmentStatus == EmploymentStatus.ACTIVE;
    }

    public EmployeeProfileId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public BranchId getBranchId() {
        return branchId;
    }

    public TenantMembershipId getMembershipId() {
        return membershipId;
    }

    public WorkShiftId getAssignedShiftId() {
        return assignedShiftId;
    }

    public Money getBaseSalary() {
        return baseSalary;
    }

    public CompensationType getCompensationType() {
        return compensationType;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }
}
