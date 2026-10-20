package com.andeva.atelier.platform.operations.application.documentservices;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

/**
 * Customer-facing documents of a work order: the commercial quotation and the vehicle exit pass.
 */
public interface WorkOrderDocumentService {

    /** US24: quotation PDF with workshop fiscal data, vehicle, labor and parts breakdown, IGV and validity. */
    Result<byte[], ApplicationError> quotationPdf(TenantId tenantId, WorkOrderId workOrderId);

    /** US24: emails the quotation PDF to the customer's contact email. */
    Result<String, ApplicationError> sendQuotation(TenantId tenantId, WorkOrderId workOrderId);

    /** US15: emails a pending defect proposal with its photo evidence to the customer for authorization. */
    Result<String, ApplicationError> notifyProposal(TenantId tenantId, WorkOrderId workOrderId, UUID proposalId);

    /** US39: exit pass, only for orders already paid. */
    Result<byte[], ApplicationError> exitPassPdf(TenantId tenantId, WorkOrderId workOrderId);
}
