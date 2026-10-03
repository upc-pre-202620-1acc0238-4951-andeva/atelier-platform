package com.andeva.atelier.platform.billing.interfaces.acl.dto;

import java.io.Serializable;

/**
 * Immutable DTO representing effective operational resource quotas and feature gates
 * exposed by the Billing Open Host Service (OHS).
 *
 * @author Joel Huamani Estefanero
 */
public record TenantQuotaLimitsDto(
        int maxBranches,
        int maxActiveStaff,
        int maxActiveObd2Devices,
        int maxPhotosPerWorkOrder,
        int maxMonthlyAiReports,
        boolean companyRegistrationAllowed,
        boolean multiWarehouseAllowed,
        boolean marketplaceListed,
        int maxMonthlyWorkOrders,
        boolean iotTelemetryEnabled,
        boolean aiDiagnosticsEnabled
) implements Serializable {
}
