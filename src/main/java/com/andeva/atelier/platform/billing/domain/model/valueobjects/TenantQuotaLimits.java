package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Immutable value object holding operational quotas and feature permissions enforced on a tenant.
 * A limit of -1 indicates unlimited capacity.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantQuotaLimits(
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

    public TenantQuotaLimits {
        if (maxBranches < -1) {
            throw new IllegalArgumentException("maxBranches must be -1 (unlimited) or non-negative");
        }
        if (maxActiveStaff < -1) {
            throw new IllegalArgumentException("maxActiveStaff must be -1 (unlimited) or non-negative");
        }
        if (maxActiveObd2Devices < -1) {
            throw new IllegalArgumentException("maxActiveObd2Devices must be -1 (unlimited) or non-negative");
        }
        if (maxPhotosPerWorkOrder < -1) {
            throw new IllegalArgumentException("maxPhotosPerWorkOrder must be -1 (unlimited) or non-negative");
        }
        if (maxMonthlyAiReports < -1) {
            throw new IllegalArgumentException("maxMonthlyAiReports must be -1 (unlimited) or non-negative");
        }
        if (maxMonthlyWorkOrders < -1) {
            throw new IllegalArgumentException("maxMonthlyWorkOrders must be -1 (unlimited) or non-negative");
        }
    }

    public static TenantQuotaLimits goPreset() {
        return new TenantQuotaLimits(
                1,
                3,
                0,
                5,
                0,
                false,
                false,
                false,
                50,
                false,
                false
        );
    }

    public static TenantQuotaLimits proPreset() {
        return new TenantQuotaLimits(
                2,
                10,
                5,
                20,
                5,
                true,
                false,
                true,
                200,
                true,
                true
        );
    }

    public static TenantQuotaLimits maxPreset() {
        return new TenantQuotaLimits(
                5,
                25,
                20,
                -1,
                50,
                true,
                true,
                true,
                1000,
                true,
                true
        );
    }

    public static TenantQuotaLimits enterprisePreset() {
        return new TenantQuotaLimits(
                -1,
                -1,
                -1,
                -1,
                -1,
                true,
                true,
                true,
                -1,
                true,
                true
        );
    }

    public static TenantQuotaLimits defaults() {
        return new TenantQuotaLimits(0, 0, 0, 0, 0, false, false, false, 0, false, false);
    }

    public static TenantQuotaLimits goPlanPreset() {
        return goPreset();
    }

    public static TenantQuotaLimits proPlanPreset() {
        return proPreset();
    }

    public static TenantQuotaLimits maxPlanPreset() {
        return maxPreset();
    }

    public static TenantQuotaLimits enterprisePlanPreset() {
        return enterprisePreset();
    }
}
