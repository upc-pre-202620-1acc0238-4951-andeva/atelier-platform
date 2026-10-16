package com.andeva.atelier.platform.iot.infrastructure.external.acl.billing;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("IoT Subscription Quota Adapter Tests")
class IoTSubscriptionQuotaAdapterTest {

    private final SubscriptionContextFacade facade = mock(SubscriptionContextFacade.class);
    private final IoTSubscriptionQuotaAdapter adapter = new IoTSubscriptionQuotaAdapter(facade);
    private final TenantId tenantId = TenantId.generate();

    private static TenantQuotaLimitsDto limits(int maxDevices, int maxAiReports) {
        return new TenantQuotaLimitsDto(-1, -1, maxDevices, -1, maxAiReports, true, true, true, -1, true, true);
    }

    @Test
    @DisplayName("Unlimited plans (-1) never block device registration or AI reports")
    void unlimitedQuotasNeverBlock() {
        when(facade.getTenantQuotaLimits(tenantId.value())).thenReturn(limits(-1, -1));

        assertThatCode(() -> adapter.validateObd2DeviceRegistrationAllowed(tenantId, 500)).doesNotThrowAnyException();
        assertThatCode(() -> adapter.validateAiReportGenerationAllowed(tenantId, 500)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Finite quotas block once the limit is reached")
    void finiteQuotasBlockAtLimit() {
        when(facade.getTenantQuotaLimits(tenantId.value())).thenReturn(limits(15, 60));

        assertThatCode(() -> adapter.validateObd2DeviceRegistrationAllowed(tenantId, 14)).doesNotThrowAnyException();
        assertThatThrownBy(() -> adapter.validateObd2DeviceRegistrationAllowed(tenantId, 15)).isInstanceOf(QuotaExceededException.class);
        assertThatThrownBy(() -> adapter.validateAiReportGenerationAllowed(tenantId, 60)).isInstanceOf(QuotaExceededException.class);
    }
}
