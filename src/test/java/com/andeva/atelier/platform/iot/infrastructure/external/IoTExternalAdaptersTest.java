package com.andeva.atelier.platform.iot.infrastructure.external;

import com.andeva.atelier.platform.billing.domain.exceptions.QuotaExceededException;
import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.SubsystemEvaluationDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.infrastructure.external.acl.billing.IoTSubscriptionQuotaAdapter;
import com.andeva.atelier.platform.iot.infrastructure.external.acl.crm.CrmFleetAclAdapter;
import com.andeva.atelier.platform.iot.infrastructure.external.acl.mro.WorkshopOperationsAclAdapter;
import com.andeva.atelier.platform.iot.infrastructure.external.messaging.outbox.IoTOutboxMessageRelayAdapter;
import com.andeva.atelier.platform.iot.infrastructure.external.notification.firebase.FirebaseCloudMessagingGatewayAdapter;
import com.andeva.atelier.platform.iot.infrastructure.external.reporting.openpdf.OpenPdfVehicleHealthReportGeneratorAdapter;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IoT External Adapters and Gateways.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class IoTExternalAdaptersTest {

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();

    @Mock
    private SubscriptionContextFacade subscriptionContextFacade;
    @Mock
    private OutboxMessageJpaRepository outboxRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("IoTSubscriptionQuotaAdapter should allow within bounds and reject when quota exceeded")
    void testSubscriptionQuotaAdapter() {
        var adapter = new IoTSubscriptionQuotaAdapter(subscriptionContextFacade);

        TenantQuotaLimitsDto allowedLimits = new TenantQuotaLimitsDto(
                5, 10, 5, 10, 60, true, true, true, 50, true, true
        );
        when(subscriptionContextFacade.getTenantQuotaLimits(tenantId.value())).thenReturn(allowedLimits);

        // Within quota
        adapter.validateObd2DeviceRegistrationAllowed(tenantId, 3);
        adapter.validateAiReportGenerationAllowed(tenantId, 45);

        // Exceeded OBD2 quota
        assertThatThrownBy(() -> adapter.validateObd2DeviceRegistrationAllowed(tenantId, 5))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("maximum active OBD-II scanners quota");

        // Exceeded AI quota
        assertThatThrownBy(() -> adapter.validateAiReportGenerationAllowed(tenantId, 60))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("monthly AI report generation quota");

        // Disabled telemetry
        TenantQuotaLimitsDto disabledLimits = new TenantQuotaLimitsDto(
                5, 10, 0, 10, 0, false, false, false, 50, false, false
        );
        when(subscriptionContextFacade.getTenantQuotaLimits(tenantId.value())).thenReturn(disabledLimits);

        assertThatThrownBy(() -> adapter.validateObd2DeviceRegistrationAllowed(tenantId, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("disabled");

        assertThatThrownBy(() -> adapter.validateAiReportGenerationAllowed(tenantId, 0))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    @DisplayName("CrmFleetAclAdapter should provide simulated tokens, metadata, and appointments")
    void testCrmFleetAclAdapter() {
        var adapter = new CrmFleetAclAdapter();

        assertThat(adapter.isVehicleRegistered(vehicleId)).isTrue();
        assertThat(adapter.isVehicleRegistered(null)).isFalse();

        String token = adapter.getDriverFcmDeviceToken(vehicleId);
        assertThat(token).isNotNull().startsWith("fcm_token_simulated_");
        assertThat(adapter.getDriverFcmDeviceToken(null)).isNull();

        adapter.registerDriverFcmToken(vehicleId.value(), "custom-token-xyz");
        assertThat(adapter.getDriverFcmDeviceToken(vehicleId)).isEqualTo("custom-token-xyz");

        UUID appointmentId = adapter.convertAlertToAppointment(
                AlertId.generate(), tenantId, vehicleId, "Warning message", null
        );
        assertThat(appointmentId).isNotNull();

        var meta = adapter.getVehicleMetadata(vehicleId);
        assertThat(meta).isPresent();
        assertThat(meta.get().brand()).isEqualTo("Toyota");

        assertThat(adapter.getVehicleMetadata(null)).isEmpty();
    }

    @Test
    @DisplayName("WorkshopOperationsAclAdapter should return standard MRO catalog items")
    void testWorkshopOperationsAclAdapter() {
        var adapter = new WorkshopOperationsAclAdapter();
        var services = adapter.getAvailableWorkshopServices(tenantId);
        assertThat(services).hasSize(4);
        assertThat(services.get(0).serviceCode()).isEqualTo("SRV-COOL-01");
    }

    @Test
    @DisplayName("FirebaseCloudMessagingGatewayAdapter should handle simulated dispatch when Firebase uninitialized")
    void testFirebaseCloudMessagingGatewayAdapter() {
        var crmAdapter = new CrmFleetAclAdapter();
        var fcmAdapter = new FirebaseCloudMessagingGatewayAdapter(crmAdapter);

        // Vehicle with mock token -> simulated send (since FirebaseApp is not initialized in unit tests)
        String msgId = fcmAdapter.sendHighPriorityNotification(
                vehicleId, "Alert", "Critical engine temp", Map.of("severity", "CRITICAL")
        );
        assertThat(msgId).isNotNull().startsWith("simulated-fcm-");

        // Missing token -> returns null
        var emptyCrm = mock(CrmFleetAclPort.class);
        when(emptyCrm.getDriverFcmDeviceToken(vehicleId)).thenReturn(null);
        var fcmNoToken = new FirebaseCloudMessagingGatewayAdapter(emptyCrm);
        assertThat(fcmNoToken.sendHighPriorityNotification(vehicleId, "Title", "Body", Map.of())).isNull();
    }

    @Test
    @DisplayName("OpenPdfVehicleHealthReportGeneratorAdapter should generate valid PDF binary bytes")
    void testOpenPdfVehicleHealthReportGeneratorAdapter() {
        var adapter = new OpenPdfVehicleHealthReportGeneratorAdapter();

        VehicleHealthReportAiDto report = new VehicleHealthReportAiDto(
                88,
                "GOOD",
                "Engine operating efficiently with minor carbon buildup.",
                List.of(new SubsystemEvaluationDto("POWERTRAIN", "OPTIMAL", 90, "Smooth timing")),
                List.of(),
                List.of(new RecommendedServiceActionDto("SRV-TUNE", "Tune up", "URGENT", BigDecimal.valueOf(120.00), "Inspect soon")),
                List.of()
        );

        CrmFleetAclPort.VehicleMetadataDto metadata = new CrmFleetAclPort.VehicleMetadataDto(
                "XYZ-999", "1HGCR2F83HA123456", "Honda", "Civic", 2021, "Jane Doe"
        );

        byte[] pdfBytes = adapter.generateHealthReportPdf(report, metadata);
        assertThat(pdfBytes).isNotNull().isNotEmpty();

        // PDF signature check (%PDF-)
        String header = new String(pdfBytes, 0, 5);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("IoTOutboxMessageRelayAdapter should poll and publish pending outbox messages")
    void testIoTOutboxMessageRelayAdapter() {
        var relay = new IoTOutboxMessageRelayAdapter(outboxRepository, eventPublisher);

        OutboxMessagePersistenceEntity message = OutboxMessagePersistenceEntity.pendingOf(
                "IoTTelemetry",
                "alert-123",
                "PredictiveAlertGeneratedIntegrationEvent",
                "{}",
                Instant.now()
        );
        message.setId(UUID.randomUUID());

        when(outboxRepository.findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(
                eq(OutboxStatus.PENDING), eq("IoTTelemetry")
        )).thenReturn(List.of(message));

        relay.relayPendingIoTMessages();

        assertThat(message.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        verify(eventPublisher).publishEvent(message);
        verify(outboxRepository).save(message);
    }
}
