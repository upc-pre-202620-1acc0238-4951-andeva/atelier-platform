package com.andeva.atelier.platform.invoicing.domain.repositories;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link VoucherPayment} child entities.
 *
 * @author Joel Huamani Estefanero
 */
public interface VoucherPaymentRepository {

    VoucherPayment save(VoucherPayment payment);

    Optional<VoucherPayment> findById(PaymentId id);

    List<VoucherPayment> findAllByVoucherId(VoucherId voucherId);

    List<VoucherPayment> findAllByBranchIdAndDate(BranchId branchId, LocalDate date);

    List<VoucherPayment> findByBranchIdAndDateRange(BranchId branchId, Instant from, Instant to);

    List<VoucherPayment> findByTenantIdAndDateRange(com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId, Instant from, Instant to);
}
