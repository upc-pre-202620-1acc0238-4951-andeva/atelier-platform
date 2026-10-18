package com.andeva.atelier.platform.crm.infrastructure.external;

import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.VerifiedAddressDto;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.crm.infrastructure.external.billing.SubscriptionValidationClient;
import com.andeva.atelier.platform.crm.infrastructure.external.firebase.DriverAppFcmClient;
import com.andeva.atelier.platform.crm.infrastructure.external.google.GooglePlacesClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying external infrastructure adapters for CRM.
 * Tests {@link SubscriptionValidationClient}, {@link DriverAppFcmClient}, and {@link GooglePlacesClient}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CRM External Adapters Unit Tests")
class CrmExternalAdaptersTest {

    @Nested
    @DisplayName("SubscriptionValidationClient Tests")
    class SubscriptionValidationClientTests {

        @Mock
        private SubscriptionContextFacade subscriptionContextFacade;

        @Test
        @DisplayName("Should validate customer quota as true when subscription is active")
        void shouldValidateCustomerQuotaWhenActive() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);
            UUID tenantId = UUID.randomUUID();
            when(subscriptionContextFacade.isTenantSubscriptionActive(tenantId)).thenReturn(true);

            boolean result = client.validateCustomerQuota(tenantId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate customer quota as false when subscription is inactive")
        void shouldRejectCustomerQuotaWhenInactive() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);
            UUID tenantId = UUID.randomUUID();
            when(subscriptionContextFacade.isTenantSubscriptionActive(tenantId)).thenReturn(false);

            boolean result = client.validateCustomerQuota(tenantId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should validate vehicle quota as true when subscription is active")
        void shouldValidateVehicleQuotaWhenActive() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);
            UUID tenantId = UUID.randomUUID();
            when(subscriptionContextFacade.isTenantSubscriptionActive(tenantId)).thenReturn(true);

            boolean result = client.validateVehicleQuota(tenantId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate vehicle quota as false when subscription is inactive")
        void shouldRejectVehicleQuotaWhenInactive() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);
            UUID tenantId = UUID.randomUUID();
            when(subscriptionContextFacade.isTenantSubscriptionActive(tenantId)).thenReturn(false);

            boolean result = client.validateVehicleQuota(tenantId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fall back to true when subscription facade throws an exception")
        void shouldFallbackWhenFacadeThrows() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);
            UUID tenantId = UUID.randomUUID();
            when(subscriptionContextFacade.isTenantSubscriptionActive(tenantId)).thenThrow(new RuntimeException("Billing service timeout"));

            assertThat(client.validateCustomerQuota(tenantId)).isTrue();
            assertThat(client.validateVehicleQuota(tenantId)).isTrue();
        }

        @Test
        @DisplayName("Should fall back to true when facade is null (resilient standalone mode)")
        void shouldFallbackWhenFacadeIsNull() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(null);
            UUID tenantId = UUID.randomUUID();

            assertThat(client.validateCustomerQuota(tenantId)).isTrue();
            assertThat(client.validateVehicleQuota(tenantId)).isTrue();
        }

        @Test
        @DisplayName("Should reject validation when tenantId is null")
        void shouldRejectWhenTenantIdIsNull() {
            SubscriptionValidationClient client = new SubscriptionValidationClient(subscriptionContextFacade);

            assertThat(client.validateCustomerQuota(null)).isFalse();
            assertThat(client.validateVehicleQuota(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("DriverAppFcmClient Tests")
    class DriverAppFcmClientTests {

        private final CustomerMembershipRepository membershipRepository = org.mockito.Mockito.mock(CustomerMembershipRepository.class);
        private final TenancyContextFacade tenancyContextFacade = org.mockito.Mockito.mock(TenancyContextFacade.class);
        private final DriverAppFcmClient fcmClient = new DriverAppFcmClient(membershipRepository, tenancyContextFacade);

        @Test
        @DisplayName("Should dispatch to devices registered by the customer's active members")
        void shouldDispatchToCustomerMemberDevices() {
            UUID customerId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            CustomerMembership membership = org.mockito.Mockito.mock(CustomerMembership.class);
            when(membership.status()).thenReturn(CustomerMembershipStatus.ACTIVE);
            when(membership.userId()).thenReturn(UserId.of(userId));
            when(membershipRepository.findByCustomerId(CustomerId.of(customerId))).thenReturn(java.util.List.of(membership));
            when(tenancyContextFacade.fetchUserDeviceToken(userId)).thenReturn(Optional.of("fcm-device-token-12345"));

            int dispatched = fcmClient.sendPushToCustomer(customerId, "Cita confirmada", "Body", Map.of());

            assertThat(dispatched).isEqualTo(1);
        }

        @Test
        @DisplayName("Should successfully dispatch push notification with valid token")
        void shouldDispatchPushWithValidToken() {
            boolean result = fcmClient.sendPushNotification(
                    "fcm-device-token-12345",
                    "Cita Confirmada",
                    "Su cita ha sido confirmada en Taller Central",
                    Map.of("appointmentId", UUID.randomUUID().toString())
            );

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should skip dispatch and return false when token is blank or null")
        void shouldRejectBlankToken() {
            assertThat(fcmClient.sendPushNotification(null, "Title", "Body", Map.of())).isFalse();
            assertThat(fcmClient.sendPushNotification("", "Title", "Body", Map.of())).isFalse();
            assertThat(fcmClient.sendPushNotification("   ", "Title", "Body", Map.of())).isFalse();
        }
    }

    @Nested
    @DisplayName("GooglePlacesClient Tests")
    class GooglePlacesClientTests {

        private final GooglePlacesClient placesClient = new GooglePlacesClient();

        @Test
        @DisplayName("Should verify address and return coordinates when address is non-empty")
        void shouldVerifyValidAddress() {
            Optional<VerifiedAddressDto> result = placesClient.verifyAddress("Av. Javier Prado Este 4571, Santiago de Surco, Lima");

            assertThat(result).isPresent();
            assertThat(result.get().formattedAddress()).contains("Av. Javier Prado Este 4571");
            assertThat(result.get().latitude()).isNotZero();
            assertThat(result.get().longitude()).isNotZero();
            assertThat(result.get().placeId()).isNotBlank();
        }

        @Test
        @DisplayName("Should return empty optional when raw address is null or blank")
        void shouldReturnEmptyForBlankAddress() {
            assertThat(placesClient.verifyAddress(null)).isEmpty();
            assertThat(placesClient.verifyAddress("")).isEmpty();
            assertThat(placesClient.verifyAddress("   ")).isEmpty();
        }
    }
}
