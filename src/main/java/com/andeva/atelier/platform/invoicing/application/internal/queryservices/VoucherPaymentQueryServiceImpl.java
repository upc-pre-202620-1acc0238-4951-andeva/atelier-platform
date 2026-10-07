package com.andeva.atelier.platform.invoicing.application.internal.queryservices;

import com.andeva.atelier.platform.invoicing.application.queryservices.VoucherPaymentQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherPaymentsQuery;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Read-only Query Service implementing voucher payments lookup and daily cash summary.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class VoucherPaymentQueryServiceImpl implements VoucherPaymentQueryService {

    private final VoucherPaymentRepository paymentRepository;

    public VoucherPaymentQueryServiceImpl(VoucherPaymentRepository paymentRepository) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository cannot be null");
    }

    @Override
    public List<VoucherPayment> handle(GetVoucherPaymentsQuery query) {
        Objects.requireNonNull(query, "GetVoucherPaymentsQuery cannot be null");
        return paymentRepository.findAllByVoucherId(query.voucherId());
    }

    @Override
    public List<VoucherPayment> getDailyPaymentsByBranch(BranchId branchId, LocalDate date) {
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");
        return paymentRepository.findAllByBranchIdAndDate(branchId, date);
    }
}
