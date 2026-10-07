package com.andeva.atelier.platform.iot.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Outbound Anti-Corruption Layer port for validating SaaS subscription plan limits and feature gates.
 *
 * @author Joel Huamani Estefanero
 */
public interface IoTSubscriptionQuotaPort {

    /**
     * Asserts that the workshop tenant is permitted to register an additional active OBD-II device.
     *
     * @param tenantId             workshop tenant identifier
     * @param currentActiveDevices number of currently active OBD-II devices for the tenant
     */
    void validateObd2DeviceRegistrationAllowed(TenantId tenantId, int currentActiveDevices);

    /**
     * Asserts that the workshop tenant is permitted to generate an additional predictive AI health report this month.
     *
     * @param tenantId              workshop tenant identifier
     * @param currentMonthlyReports number of AI reports already generated in the current billing cycle
     */
    void validateAiReportGenerationAllowed(TenantId tenantId, int currentMonthlyReports);
}
