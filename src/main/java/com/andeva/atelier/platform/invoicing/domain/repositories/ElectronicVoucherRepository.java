package com.andeva.atelier.platform.invoicing.domain.repositories;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link ElectronicVoucher} aggregate roots.
 *
 * @author Joel Huamani Estefanero
 */
public interface ElectronicVoucherRepository {

    ElectronicVoucher save(ElectronicVoucher voucher);

    Optional<ElectronicVoucher> findById(VoucherId id);

    Optional<ElectronicVoucher> findByTenantIdAndSerieAndNumber(TenantId tenantId, VoucherSerie serie, VoucherNumber number);

    List<ElectronicVoucher> findAllByTenantId(TenantId tenantId);

    List<ElectronicVoucher> findAllByTenantIdAndDateRange(TenantId tenantId, LocalDate from, LocalDate to);

    default List<ElectronicVoucher> findAllByTenantIdAndBranchIdAndDateRange(
            TenantId tenantId,
            BranchId branchId,
            LocalDate from,
            LocalDate to
    ) {
        return findAllByTenantIdAndDateRange(tenantId, from, to).stream()
                .filter(v -> branchId == null || branchId.equals(v.getBranchId()))
                .toList();
    }

    default List<ElectronicVoucher> findAllByStatus(VoucherStatus status) {
        return List.of();
    }

    List<ElectronicVoucher> findAllByWorkOrderId(WorkOrderId workOrderId);

    boolean existsByTenantIdAndSerieAndNumber(TenantId tenantId, VoucherSerie serie, VoucherNumber number);
}
