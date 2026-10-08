package com.andeva.atelier.platform.hr.application.acl;

import com.andeva.atelier.platform.hr.application.queryservices.AttendanceQueryService;
import com.andeva.atelier.platform.hr.application.queryservices.EmployeeProfileQueryService;
import com.andeva.atelier.platform.hr.application.queryservices.PayrollPaymentQueryService;
import com.andeva.atelier.platform.hr.application.queryservices.WorkShiftQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.queries.*;
import com.andeva.atelier.platform.hr.interfaces.acl.HumanResourcesContextFacade;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.AttendanceSummaryAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.EmployeeWorkShiftAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.MechanicDutyProfileAclDto;
import com.andeva.atelier.platform.hr.interfaces.acl.dto.PayrollLaborCostAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class HumanResourcesContextFacadeImpl implements HumanResourcesContextFacade {

    private final AttendanceQueryService attendanceQueryService;
    private final EmployeeProfileQueryService employeeProfileQueryService;
    private final WorkShiftQueryService workShiftQueryService;
    private final PayrollPaymentQueryService payrollPaymentQueryService;
    private final EmployeeProfileRepository employeeProfileRepository;

    public HumanResourcesContextFacadeImpl(
            AttendanceQueryService attendanceQueryService,
            EmployeeProfileQueryService employeeProfileQueryService,
            WorkShiftQueryService workShiftQueryService,
            PayrollPaymentQueryService payrollPaymentQueryService,
            EmployeeProfileRepository employeeProfileRepository) {
        this.attendanceQueryService = Objects.requireNonNull(attendanceQueryService, "attendanceQueryService cannot be null");
        this.employeeProfileQueryService = Objects.requireNonNull(employeeProfileQueryService, "employeeProfileQueryService cannot be null");
        this.workShiftQueryService = Objects.requireNonNull(workShiftQueryService, "workShiftQueryService cannot be null");
        this.payrollPaymentQueryService = Objects.requireNonNull(payrollPaymentQueryService, "payrollPaymentQueryService cannot be null");
        this.employeeProfileRepository = Objects.requireNonNull(employeeProfileRepository, "employeeProfileRepository cannot be null");
    }

    @Override
    public boolean isMechanicOnDuty(UUID tenantId, UUID membershipId) {
        return attendanceQueryService.handle(new GetTodayAttendanceByMembershipQuery(
                TenantId.of(tenantId), TenantMembershipId.of(membershipId)
        )).map(a -> a.getClockOut() == null).orElse(false);
    }

    @Override
    public Optional<UUID> getMechanicActiveBranchId(UUID tenantId, UUID membershipId) {
        return attendanceQueryService.handle(new GetTodayAttendanceByMembershipQuery(
                TenantId.of(tenantId), TenantMembershipId.of(membershipId)
        )).filter(a -> a.getClockOut() == null).map(a -> a.getBranchId().value());
    }

    @Override
    public Optional<MechanicDutyProfileAclDto> getMechanicProfile(UUID tenantId, UUID membershipId) {
        Optional<EmployeeProfile> profileOpt = employeeProfileQueryService.handle(
                new GetEmployeeProfileByMembershipIdQuery(TenantId.of(tenantId), TenantMembershipId.of(membershipId))
        );
        if (profileOpt.isEmpty()) return Optional.empty();

        EmployeeProfile profile = profileOpt.get();
        boolean onDuty = isMechanicOnDuty(tenantId, membershipId);

        return Optional.of(new MechanicDutyProfileAclDto(
                profile.getId().value(),
                profile.getMembershipId().value(),
                profile.getBranchId().value(),
                profile.getJobTitle(),
                profile.getEmploymentStatus().name(),
                onDuty
        ));
    }

    @Override
    public Optional<AttendanceSummaryAclDto> getDailyAttendanceSummary(UUID tenantId, UUID membershipId, LocalDate date) {
        Optional<AttendanceRecord> recordOpt = attendanceQueryService.handle(
                new GetTodayAttendanceByMembershipQuery(TenantId.of(tenantId), TenantMembershipId.of(membershipId))
        );
        return recordOpt.map(r -> new AttendanceSummaryAclDto(
                r.getMembershipId().value(),
                date != null ? date : LocalDate.now(),
                r.getStatus().name(),
                r.totalWorkedMinutes(),
                r.getJustification().isPresent()
        ));
    }

    @Override
    public Optional<EmployeeWorkShiftAclDto> getEmployeeWorkShift(UUID tenantId, UUID membershipId) {
        Optional<EmployeeProfile> profileOpt = employeeProfileQueryService.handle(
                new GetEmployeeProfileByMembershipIdQuery(TenantId.of(tenantId), TenantMembershipId.of(membershipId))
        );
        if (profileOpt.isEmpty() || profileOpt.get().getAssignedShiftId() == null) {
            return Optional.empty();
        }

        Optional<WorkShift> shiftOpt = workShiftQueryService.handle(
                new GetWorkShiftByIdQuery(TenantId.of(tenantId), profileOpt.get().getAssignedShiftId())
        );

        return shiftOpt.map(s -> new EmployeeWorkShiftAclDto(
                s.getId().value(),
                s.getName(),
                s.getSchedule().startTime(),
                s.getSchedule().endTime(),
                s.getGracePeriod().minutes()
        ));
    }

    @Override
    public Optional<PayrollLaborCostAclDto> getPayrollLaborCost(UUID tenantId, LocalDate periodStart, LocalDate periodEnd) {
        List<PayrollPayment> payments = payrollPaymentQueryService.handle(
                new ListPayrollPaymentsByPeriodQuery(TenantId.of(tenantId), periodStart, periodEnd, null)
        );

        BigDecimal base = BigDecimal.ZERO;
        BigDecimal bonuses = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;

        for (PayrollPayment p : payments) {
            base = base.add(p.getBaseAmount().amount());
            bonuses = bonuses.add(p.getBonuses().amount());
            deductions = deductions.add(p.getDeductions().amount());
            net = net.add(p.getTotalPaid().amount());
        }

        return Optional.of(new PayrollLaborCostAclDto(
                tenantId, periodStart, periodEnd, base, bonuses, deductions, net
        ));
    }

    @Override
    public BigDecimal calculateAccruedProductivityBonus(UUID tenantId, UUID membershipId, LocalDate periodStart, LocalDate periodEnd) {
        List<PayrollPayment> payments = payrollPaymentQueryService.handle(
                new ListPayrollPaymentsByPeriodQuery(TenantId.of(tenantId), periodStart, periodEnd, null)
        );

        return payments.stream()
                .filter(p -> p.getMembershipId().value().equals(membershipId))
                .map(p -> p.getBonuses().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<UUID> resolveTenantId(UUID membershipId) {
        if (membershipId == null) return Optional.empty();
        return employeeProfileRepository.findByMembershipId(TenantMembershipId.of(membershipId))
                .map(p -> p.getTenantId().value());
    }

    @Override
    public boolean isMechanicOnDuty(UUID membershipId) {
        return resolveTenantId(membershipId)
                .map(tenantId -> isMechanicOnDuty(tenantId, membershipId))
                .orElse(false);
    }

    @Override
    public Optional<UUID> getMechanicActiveBranchId(UUID membershipId) {
        return resolveTenantId(membershipId)
                .flatMap(tenantId -> getMechanicActiveBranchId(tenantId, membershipId));
    }

    @Override
    public Optional<MechanicDutyProfileAclDto> getMechanicProfile(UUID membershipId) {
        return resolveTenantId(membershipId)
                .flatMap(tenantId -> getMechanicProfile(tenantId, membershipId));
    }

    @Override
    public Optional<AttendanceSummaryAclDto> getDailyAttendanceSummary(UUID membershipId, LocalDate date) {
        return resolveTenantId(membershipId)
                .flatMap(tenantId -> getDailyAttendanceSummary(tenantId, membershipId, date));
    }

    @Override
    public Optional<EmployeeWorkShiftAclDto> getEmployeeWorkShift(UUID membershipId) {
        return resolveTenantId(membershipId)
                .flatMap(tenantId -> getEmployeeWorkShift(tenantId, membershipId));
    }

    @Override
    public BigDecimal calculateAccruedProductivityBonus(UUID membershipId, LocalDate periodStart, LocalDate periodEnd) {
        return resolveTenantId(membershipId)
                .map(tenantId -> calculateAccruedProductivityBonus(tenantId, membershipId, periodStart, periodEnd))
                .orElse(BigDecimal.ZERO);
    }
}