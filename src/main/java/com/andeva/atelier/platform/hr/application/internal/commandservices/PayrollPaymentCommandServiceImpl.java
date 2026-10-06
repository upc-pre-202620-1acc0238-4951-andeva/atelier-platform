package com.andeva.atelier.platform.hr.application.internal.commandservices;

import com.andeva.atelier.platform.hr.application.commandservices.PayrollPaymentCommandService;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.MroLaborCommissionAclService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.commands.*;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.hr.domain.repositories.PayrollPaymentRepository;
import com.andeva.atelier.platform.hr.domain.services.PayrollCalculationEngine;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class PayrollPaymentCommandServiceImpl implements PayrollPaymentCommandService {

    private final PayrollPaymentRepository payrollRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final MroLaborCommissionAclService commissionAclService;
    private final PayrollCalculationEngine calculationEngine;

    public PayrollPaymentCommandServiceImpl(
            PayrollPaymentRepository payrollRepository,
            EmployeeProfileRepository employeeRepository,
            AttendanceRecordRepository attendanceRepository,
            MroLaborCommissionAclService commissionAclService,
            PayrollCalculationEngine calculationEngine
    ) {
        this.payrollRepository = Objects.requireNonNull(payrollRepository, "payrollRepository cannot be null");
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository cannot be null");
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository cannot be null");
        this.commissionAclService = Objects.requireNonNull(commissionAclService, "commissionAclService cannot be null");
        this.calculationEngine = Objects.requireNonNull(calculationEngine, "calculationEngine cannot be null");
    }

    @Override
    public Result<PayrollPayment, ApplicationError> handle(GeneratePayrollCommand command) {
        Optional<EmployeeProfile> profileOpt = employeeRepository.findByMembershipId(command.membershipId());
        if (profileOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el expediente del empleado"));
        }
        EmployeeProfile profile = profileOpt.get();

        PayPeriod period = PayPeriod.of(command.periodStart(), command.periodEnd());
        var existingOpt = payrollRepository.findByMembershipAndPeriod(command.tenantId(), command.membershipId(), period);
        if (existingOpt.isPresent()) {
            return Result.failure(ApplicationError.conflict("Ya existe una liquidación de nómina para dicho periodo"));
        }

        Instant start = command.periodStart().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = command.periodEnd().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<AttendanceRecord> records = attendanceRepository.findAllByMembershipAndPeriod(
                command.tenantId(), command.membershipId(), start, end
        );

        var commissionDtos = commissionAclService.getAccruedCommissions(
                command.tenantId(), command.membershipId(), command.periodStart(), command.periodEnd()
        );
        List<PayrollCalculationEngine.CommissionEntry> commissionEntries = commissionDtos.stream()
                .map(c -> new PayrollCalculationEngine.CommissionEntry(c.taskDescription(), c.commissionAmount(), c.completedDate()))
                .toList();

        PayrollPayment payment = calculationEngine.calculatePayroll(
                command.tenantId(), command.membershipId(), period, profile.getBaseSalary(), records, commissionEntries
        );

        PayrollPayment saved = payrollRepository.save(payment);
        return Result.success(saved);
    }

    @Override
    public Result<PayrollPayment, ApplicationError> handle(AddPayrollDeductionCommand command) {
        Optional<PayrollPayment> paymentOpt = payrollRepository.findById(command.payrollId());
        if (paymentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la liquidación de nómina"));
        }

        PayrollPayment payment = paymentOpt.get();
        if (!payment.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("La liquidación pertenece a otro taller"));
        }

        try {
            payment.addDeduction(command.concept(), command.amount(), command.deductionType(), command.date());
            PayrollPayment saved = payrollRepository.save(payment);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PayrollPayment, ApplicationError> handle(AddPayrollBonusCommand command) {
        Optional<PayrollPayment> paymentOpt = payrollRepository.findById(command.payrollId());
        if (paymentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la liquidación de nómina"));
        }

        PayrollPayment payment = paymentOpt.get();
        if (!payment.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("La liquidación pertenece a otro taller"));
        }

        try {
            payment.addBonus(command.concept(), command.amount(), command.bonusType(), command.date());
            PayrollPayment saved = payrollRepository.save(payment);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PayrollPayment, ApplicationError> handle(ApprovePayrollCommand command) {
        Optional<PayrollPayment> paymentOpt = payrollRepository.findById(command.payrollId());
        if (paymentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la liquidación de nómina"));
        }

        PayrollPayment payment = paymentOpt.get();
        if (!payment.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("La liquidación pertenece a otro taller"));
        }

        try {
            payment.approve(command.approverMembershipId());
            PayrollPayment saved = payrollRepository.save(payment);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<PayrollPayment, ApplicationError> handle(DisbursePayrollPaymentCommand command) {
        Optional<PayrollPayment> paymentOpt = payrollRepository.findById(command.payrollId());
        if (paymentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la liquidación de nómina"));
        }

        PayrollPayment payment = paymentOpt.get();
        if (!payment.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("La liquidación pertenece a otro taller"));
        }

        try {
            payment.disburse(command.paymentReference(), command.paidAt());
            PayrollPayment saved = payrollRepository.save(payment);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }
}
