package com.andeva.atelier.platform.hr.interfaces.acl;

import com.andeva.atelier.platform.hr.interfaces.acl.dto.AttendanceSummaryAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.EmployeeWorkShiftAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.MechanicDutyProfileAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.PayrollLaborCostAclDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Joel Huamani Estefanero
 */
public interface HumanResourcesContextFacade {
    boolean isMechanicOnDuty(UUID tenantId, UUID membershipId);
    Optional<UUID> getMechanicActiveBranchId(UUID tenantId, UUID membershipId);
    Optional<MechanicDutyProfileAclDto> getMechanicProfile(UUID tenantId, UUID membershipId);
    Optional<AttendanceSummaryAclDto> getDailyAttendanceSummary(UUID tenantId, UUID membershipId, LocalDate date);
    Optional<EmployeeWorkShiftAclDto> getEmployeeWorkShift(UUID tenantId, UUID membershipId);
    Optional<PayrollLaborCostAclDto> getPayrollLaborCost(UUID tenantId, LocalDate periodStart, LocalDate periodEnd);
    BigDecimal calculateAccruedProductivityBonus(UUID tenantId, UUID membershipId, LocalDate periodStart, LocalDate periodEnd);

    boolean isMechanicOnDuty(UUID membershipId);
    Optional<UUID> getMechanicActiveBranchId(UUID membershipId);
    Optional<MechanicDutyProfileAclDto> getMechanicProfile(UUID membershipId);
    Optional<AttendanceSummaryAclDto> getDailyAttendanceSummary(UUID membershipId, LocalDate date);
    Optional<EmployeeWorkShiftAclDto> getEmployeeWorkShift(UUID membershipId);
    BigDecimal calculateAccruedProductivityBonus(UUID membershipId, LocalDate periodStart, LocalDate periodEnd);
}