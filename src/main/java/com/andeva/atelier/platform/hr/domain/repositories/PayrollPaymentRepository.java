package com.andeva.atelier.platform.hr.domain.repositories;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PayrollPaymentRepository {
    PayrollPayment save(PayrollPayment payrollPayment);
    Optional<PayrollPayment> findById(PayrollPaymentId id);
    Optional<PayrollPayment> findByMembershipAndPeriod(TenantId tenantId, TenantMembershipId membershipId, PayPeriod period);
    List<PayrollPayment> findAllByTenantIdAndPeriod(TenantId tenantId, LocalDate start, LocalDate end);
    List<PayrollPayment> findAllByTenantId(TenantId tenantId);
    List<PayrollPayment> findAllByMembershipId(TenantId tenantId, TenantMembershipId membershipId);
}
