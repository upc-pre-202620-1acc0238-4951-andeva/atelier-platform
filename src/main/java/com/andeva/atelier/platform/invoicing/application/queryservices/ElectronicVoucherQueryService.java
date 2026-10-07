package com.andeva.atelier.platform.invoicing.application.queryservices;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherBySerieAndNumberQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVouchersByTenantQuery;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

import java.util.List;
import java.util.Optional;

/**
 * Query Service interface for retrieving electronic vouchers and tax documents.
 *
 * @author Joel Huamani Estefanero
 */
public interface ElectronicVoucherQueryService {

    /**
     * Retrieves an electronic voucher by its unique identifier.
     */
    Optional<ElectronicVoucher> handle(GetVoucherByIdQuery query);

    /**
     * Retrieves an electronic voucher by tenant ID, series, and correlative number.
     */
    Optional<ElectronicVoucher> handle(GetVoucherBySerieAndNumberQuery query);

    /**
     * Retrieves all electronic vouchers for a tenant within an optional date range.
     */
    List<ElectronicVoucher> handle(GetVouchersByTenantQuery query);

    /**
     * Retrieves all electronic vouchers issued against an MRO Work Order.
     */
    List<ElectronicVoucher> getVouchersByWorkOrderId(WorkOrderId workOrderId);
}
