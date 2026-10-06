package com.andeva.atelier.platform.hr.interfaces.rest.transform;

import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.commands.AssignShiftToEmployeeCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RegisterEmployeeProfileCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateEmploymentStatusCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateSalaryCommand;
import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AssignShiftRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.RegisterEmployeeProfileRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateEmploymentStatusRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateSalaryRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.EmployeeProfileResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.UUID;

public final class EmployeeProfileResourceAssembler {

    private EmployeeProfileResourceAssembler() {}

    public static RegisterEmployeeProfileCommand toCommand(TenantId tenantId, RegisterEmployeeProfileRequest request) {
        Currency cur = request.currency() != null ? Currency.valueOf(request.currency().toUpperCase()) : Currency.PEN;
        CompensationType compType = request.compensationType() != null
                ? CompensationType.valueOf(request.compensationType().toUpperCase())
                : CompensationType.MONTHLY_FIXED;
        WorkShiftId shiftId = request.shiftId() != null ? WorkShiftId.of(request.shiftId()) : null;

        return new RegisterEmployeeProfileCommand(
                tenantId,
                BranchId.of(request.branchId()),
                TenantMembershipId.of(request.membershipId()),
                shiftId,
                Money.of(request.baseSalary(), cur),
                compType,
                request.jobTitle()
        );
    }

    public static AssignShiftToEmployeeCommand toCommand(TenantId tenantId, EmployeeProfileId profileId, AssignShiftRequest request) {
        return new AssignShiftToEmployeeCommand(tenantId, profileId, WorkShiftId.of(request.shiftId()));
    }

    public static UpdateSalaryCommand toCommand(TenantId tenantId, EmployeeProfileId profileId, UpdateSalaryRequest request) {
        Currency cur = request.currency() != null ? Currency.valueOf(request.currency().toUpperCase()) : Currency.PEN;
        CompensationType compType = request.compensationType() != null
                ? CompensationType.valueOf(request.compensationType().toUpperCase())
                : CompensationType.MONTHLY_FIXED;
        return new UpdateSalaryCommand(tenantId, profileId, Money.of(request.baseSalary(), cur), compType);
    }

    public static UpdateEmploymentStatusCommand toCommand(TenantId tenantId, EmployeeProfileId profileId, UpdateEmploymentStatusRequest request) {
        EmploymentStatus status = EmploymentStatus.valueOf(request.employmentStatus().toUpperCase());
        return new UpdateEmploymentStatusCommand(tenantId, profileId, status);
    }

    public static EmployeeProfileResource toResource(EmployeeProfile domain) {
        if (domain == null) return null;
        UUID shiftId = domain.getAssignedShiftId() != null ? domain.getAssignedShiftId().value() : null;

        return new EmployeeProfileResource(
                domain.getId().value(),
                domain.getBranchId().value(),
                domain.getMembershipId().value(),
                shiftId,
                domain.getBaseSalary().amount(),
                domain.getBaseSalary().currency().name(),
                domain.getCompensationType().name(),
                domain.getJobTitle(),
                domain.getEmploymentStatus().name()
        );
    }

    public static List<EmployeeProfileResource> toResourceList(List<EmployeeProfile> list) {
        if (list == null) return List.of();
        return list.stream().map(EmployeeProfileResourceAssembler::toResource).toList();
    }
}
