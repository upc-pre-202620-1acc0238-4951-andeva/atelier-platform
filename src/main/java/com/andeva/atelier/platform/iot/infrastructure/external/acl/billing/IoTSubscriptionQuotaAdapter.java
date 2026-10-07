package com.andeva.atelier.platform.iot.infrastructure.external.acl.billing;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Anti-Corruption Layer adapter for validating operational quotas and subscription gates
 * by querying the {@link SubscriptionContextFacade} from the SaaS Billing Bounded Context.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class IoTSubscriptionQuotaAdapter implements IoTSubscriptionQuotaPort {

    private final SubscriptionContextFacade subscriptionContextFacade;

    public IoTSubscriptionQuotaAdapter(SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    @Override
    public void validateObd2DeviceRegistrationAllowed(TenantId tenantId, int currentActiveDevices) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        TenantQuotaLimitsDto limits = subscriptionContextFacade.getTenantQuotaLimits(tenantId.value());

        if (!limits.iotTelemetryEnabled()) {
            throw new QuotaExceededException(String.format(
                    "IoT Telemetry is disabled for the current subscription plan of tenant %s",
                    tenantId.value()
            ));
        }

        if (currentActiveDevices >= limits.maxActiveObd2Devices()) {
            throw new QuotaExceededException(String.format(
                    "Tenant %s has reached the maximum active OBD-II scanners quota (%d/%d)",
                    tenantId.value(), currentActiveDevices, limits.maxActiveObd2Devices()
            ));
        }
    }

    @Override
    public void validateAiReportGenerationAllowed(TenantId tenantId, int currentMonthlyReports) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        TenantQuotaLimitsDto limits = subscriptionContextFacade.getTenantQuotaLimits(tenantId.value());

        if (!limits.aiDiagnosticsEnabled()) {
            throw new QuotaExceededException(String.format(
                    "AI Diagnostics is disabled for the current subscription plan of tenant %s",
                    tenantId.value()
            ));
        }

        if (currentMonthlyReports >= limits.maxMonthlyAiReports()) {
            throw new QuotaExceededException(String.format(
                    "Tenant %s has reached the monthly AI report generation quota (%d/%d)",
                    tenantId.value(), currentMonthlyReports, limits.maxMonthlyAiReports()
            ));
        }
    }
}
