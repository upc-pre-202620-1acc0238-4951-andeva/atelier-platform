package com.andeva.atelier.platform.hr.domain.services;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class PayrollCalculationEngine {

    public PayrollPayment calculatePayroll(
            TenantId tenantId,
            TenantMembershipId membershipId,
            PayPeriod period,
            Money baseSalary,
            List<AttendanceRecord> records,
            List<CommissionEntry> commissions
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(period, "PayPeriod cannot be null");
        Objects.requireNonNull(baseSalary, "baseSalary cannot be null");

        PayrollPayment payment = PayrollPayment.calculate(tenantId, membershipId, period, baseSalary);

        if (records != null) {
            BigDecimal dailySalary = baseSalary.amount().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_EVEN);
            BigDecimal minuteRate = dailySalary.divide(BigDecimal.valueOf(480), 4, RoundingMode.HALF_EVEN); // 8h = 480m

            for (AttendanceRecord record : records) {
                if (record.getStatus() == AttendanceStatus.ABSENT) {
                    payment.addDeduction(
                            "Descuento por inasistencia injustificada: " + record.getClockIn(),
                            Money.of(dailySalary, baseSalary.currency()),
                            "UNJUSTIFIED_ABSENCE",
                            LocalDate.now()
                    );
                } else if (record.getStatus() == AttendanceStatus.LATE) {
                    // Si hubo tardanza y no fue justificada
                    BigDecimal penalty = minuteRate.multiply(BigDecimal.valueOf(15)).setScale(2, RoundingMode.HALF_EVEN);
                    payment.addDeduction(
                            "Penalidad por tardanza no justificada",
                            Money.of(penalty, baseSalary.currency()),
                            "TARDINESS",
                            LocalDate.now()
                    );
                }
            }
        }

        if (commissions != null) {
            for (CommissionEntry entry : commissions) {
                payment.addBonus(
                        entry.concept(),
                        entry.amount(),
                        "WORK_ORDER_COMMISSION",
                        entry.date()
                );
            }
        }

        return payment;
    }

    public record CommissionEntry(String concept, Money amount, LocalDate date) {}
}
