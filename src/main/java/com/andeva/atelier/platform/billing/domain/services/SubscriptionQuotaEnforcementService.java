package com.andeva.atelier.platform.billing.domain.services;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Domain Service responsible for validating and strictly enforcing operational resource quotas
 * and feature gates defined by a workshop tenant's commercial subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class SubscriptionQuotaEnforcementService {

    public void validateBranchCreationAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentBranchCount) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (limits.maxBranches() != -1 && currentBranchCount >= limits.maxBranches()) {
            throw new QuotaExceededException(String.format(
                    "Branch limit reached (%d/%d allowed branches). Please upgrade your subscription plan to open additional locations.",
                    currentBranchCount, limits.maxBranches()
            ));
        }
    }

    public void validateStaffAdditionAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentStaffCount) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (limits.maxActiveStaff() != -1 && currentStaffCount >= limits.maxActiveStaff()) {
            throw new QuotaExceededException(String.format(
                    "Staff limit reached (%d/%d active staff members). Please upgrade your subscription plan to add more team members.",
                    currentStaffCount, limits.maxActiveStaff()
            ));
        }
    }

    public void validateWorkOrderCreationAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentMonthlyWorkOrders) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (limits.maxMonthlyWorkOrders() != -1 && currentMonthlyWorkOrders >= limits.maxMonthlyWorkOrders()) {
            throw new QuotaExceededException(String.format(
                    "Monthly work order quota reached (%d/%d work orders). Please upgrade your plan tier to continue accepting vehicles this month.",
                    currentMonthlyWorkOrders, limits.maxMonthlyWorkOrders()
            ));
        }
    }

    public void validateObd2DeviceRegistrationAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentActiveObd2Devices) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (!limits.iotTelemetryEnabled()) {
            throw new QuotaExceededException("Live OBD-II IoT telemetry is not enabled on your current subscription plan.");
        }
        if (limits.maxActiveObd2Devices() != -1 && currentActiveObd2Devices >= limits.maxActiveObd2Devices()) {
            throw new QuotaExceededException(String.format(
                    "Active OBD-II dongle quota reached (%d/%d connected scanners). Please upgrade your plan to register additional devices.",
                    currentActiveObd2Devices, limits.maxActiveObd2Devices()
            ));
        }
    }

    public void validatePhotoUploadAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentPhotosInWorkOrder) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (limits.maxPhotosPerWorkOrder() != -1 && currentPhotosInWorkOrder >= limits.maxPhotosPerWorkOrder()) {
            throw new QuotaExceededException(String.format(
                    "Photo attachment limit reached for this work order (%d/%d photos). Please upgrade your plan tier for higher attachment limits.",
                    currentPhotosInWorkOrder, limits.maxPhotosPerWorkOrder()
            ));
        }
    }

    public void validateCompanyCustomerRegistrationAllowed(TenantSubscription subscription, SubscriptionPlan plan) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (!limits.companyRegistrationAllowed()) {
            throw new QuotaExceededException(
                    "Registration of corporate business customers (RUC) requires the Max or Enterprise plan. Your current plan only supports individual customers (DNI/CE)."
            );
        }
    }

    public void validateAiReportGenerationAllowed(TenantSubscription subscription, SubscriptionPlan plan, int currentMonthlyAiReports) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (!limits.aiDiagnosticsEnabled() || limits.maxMonthlyAiReports() <= 0) {
            throw new QuotaExceededException(
                    "Predictive AI-assisted Vehicle Health Reports require the Max or Enterprise subscription plan."
            );
        }
        if (limits.maxMonthlyAiReports() != -1 && currentMonthlyAiReports >= limits.maxMonthlyAiReports()) {
            throw new QuotaExceededException(String.format(
                    "Monthly predictive AI reports quota reached (%d/%d reports generated this month).",
                    currentMonthlyAiReports, limits.maxMonthlyAiReports()
            ));
        }
    }

    public void validateMultiWarehouseTransferAllowed(TenantSubscription subscription, SubscriptionPlan plan) {
        verifySubscriptionActive(subscription);
        TenantQuotaLimits limits = plan.quotaLimits();
        if (!limits.multiWarehouseAllowed()) {
            throw new QuotaExceededException(
                    "Multi-warehouse inventory routing and inter-branch transfers require the Max or Enterprise plan with ERP Suite."
            );
        }
    }

    public boolean isFeatureEnabled(TenantSubscription subscription, SubscriptionPlan plan, String featureKey) {
        if (subscription == null || !subscription.isAccessGranted() || plan == null) {
            return false;
        }
        return plan.hasFeature(featureKey);
    }

    private void verifySubscriptionActive(TenantSubscription subscription) {
        Objects.requireNonNull(subscription, "TenantSubscription cannot be null");
        if (!subscription.isAccessGranted()) {
            throw new QuotaExceededException("Workshop tenant subscription is inactive, suspended, or past due outside grace period.");
        }
    }
}
