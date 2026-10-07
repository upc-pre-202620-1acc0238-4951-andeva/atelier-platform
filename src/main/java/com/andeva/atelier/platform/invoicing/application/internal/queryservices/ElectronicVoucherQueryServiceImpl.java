package com.andeva.atelier.platform.invoicing.application.internal.queryservices;

import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherBySerieAndNumberQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVouchersByTenantQuery;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Read-only Query Service implementing electronic voucher lookup and catalog filtering.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class ElectronicVoucherQueryServiceImpl implements ElectronicVoucherQueryService {

    private final ElectronicVoucherRepository voucherRepository;

    public ElectronicVoucherQueryServiceImpl(ElectronicVoucherRepository voucherRepository) {
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "Voucher repository cannot be null");
    }

    @Override
    public Optional<ElectronicVoucher> handle(GetVoucherByIdQuery query) {
        Objects.requireNonNull(query, "GetVoucherByIdQuery cannot be null");
        return voucherRepository.findById(query.voucherId());
    }

    @Override
    public Optional<ElectronicVoucher> handle(GetVoucherBySerieAndNumberQuery query) {
        Objects.requireNonNull(query, "GetVoucherBySerieAndNumberQuery cannot be null");
        return voucherRepository.findByTenantIdAndSerieAndNumber(
                query.tenantId(),
                query.serie(),
                query.number()
        );
    }

    @Override
    public List<ElectronicVoucher> handle(GetVouchersByTenantQuery query) {
        Objects.requireNonNull(query, "GetVouchersByTenantQuery cannot be null");
        LocalDate from = query.from() != null ? query.from() : LocalDate.now().minusMonths(1);
        LocalDate to = query.to() != null ? query.to() : LocalDate.now();

        List<ElectronicVoucher> vouchers = voucherRepository.findAllByTenantIdAndDateRange(query.tenantId(), from, to);

        if (query.type().isPresent()) {
            return vouchers.stream()
                    .filter(v -> v.getVoucherType() == query.type().get())
                    .toList();
        }

        return vouchers;
    }

    @Override
    public List<ElectronicVoucher> getVouchersByWorkOrderId(WorkOrderId workOrderId) {
        Objects.requireNonNull(workOrderId, "WorkOrderId cannot be null");
        return voucherRepository.findAllByWorkOrderId(workOrderId);
    }
}
