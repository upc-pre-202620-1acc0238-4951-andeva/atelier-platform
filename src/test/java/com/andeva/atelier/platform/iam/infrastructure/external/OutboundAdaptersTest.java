package com.andeva.atelier.platform.iam.infrastructure.external;

import com.andeva.atelier.platform.iam.application.internal.dto.GoogleUserPayload;
import com.andeva.atelier.platform.iam.infrastructure.external.google.GoogleIdentityAdapter;
import com.andeva.atelier.platform.iam.infrastructure.external.quota.SubscriptionQuotaAdapter;
import com.andeva.atelier.platform.iam.infrastructure.external.resend.ResendEmailAdapter;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for external outbound integration adapters (Resend, Google Identity, Subscription Quota).
 *
 * @author Joel Huamani Estefanero
 */
class OutboundAdaptersTest {

    @Test
    @DisplayName("ResendEmailAdapter executes dispatch without error under test configuration")
    void testResendEmailAdapter() {
        ResendEmailAdapter adapter = new ResendEmailAdapter("placeholder-key", "notifications@atelier.pe");
        EmailAddress recipient = EmailAddress.of("user@workshop.pe");

        assertThatCode(() -> adapter.sendVerificationEmail(recipient, "123456"))
                .doesNotThrowAnyException();
        assertThatCode(() -> adapter.sendPasswordResetEmail(recipient, "reset-token-xyz"))
                .doesNotThrowAnyException();
        assertThatCode(() -> adapter.sendStaffInvitationEmail(recipient, "AutoFix", "invite-token-abc"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("GoogleIdentityAdapter resolves mock tokens and handles empty inputs safely")
    void testGoogleIdentityAdapter() {
        GoogleIdentityAdapter adapter = new GoogleIdentityAdapter("google_client_id_placeholder");

        Optional<GoogleUserPayload> emptyResult = adapter.verifyIdToken("");
        assertThat(emptyResult).isEmpty();

        Optional<GoogleUserPayload> nullResult = adapter.verifyIdToken(null);
        assertThat(nullResult).isEmpty();

        Optional<GoogleUserPayload> mockResult = adapter.verifyIdToken("mock-google-token-valid");
        assertThat(mockResult).isPresent();
        assertThat(mockResult.get().email()).isEqualTo("user.mock@atelier.pe");
        assertThat(mockResult.get().givenName()).isEqualTo("Carlos");
        assertThat(mockResult.get().emailVerified()).isTrue();
    }

    @Test
    @DisplayName("SubscriptionQuotaAdapter allows additions within limit and throws when exceeded via SubscriptionContextFacade")
    void testSubscriptionQuotaAdapter() {
        com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade facade =
                org.mockito.Mockito.mock(com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade.class);
        SubscriptionQuotaAdapter adapter = new SubscriptionQuotaAdapter(facade);
        TenantId tenantId = TenantId.generate();
        com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto limits =
                new com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto(
                        3, 5, 0, 0, 0, false, false, false, 0, false, false);

        org.mockito.Mockito.when(facade.canAddBranch(tenantId.value(), 2)).thenReturn(true);
        org.mockito.Mockito.when(facade.canAddBranch(tenantId.value(), 3)).thenReturn(false);
        org.mockito.Mockito.when(facade.getTenantQuotaLimits(tenantId.value())).thenReturn(limits);

        org.mockito.Mockito.when(facade.canAddStaffMember(tenantId.value(), 4)).thenReturn(true);
        org.mockito.Mockito.when(facade.canAddStaffMember(tenantId.value(), 5)).thenReturn(false);

        assertThatCode(() -> adapter.validateBranchCreationAllowed(tenantId, 2))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> adapter.validateBranchCreationAllowed(tenantId, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum allowed branches is 3");

        assertThatCode(() -> adapter.validateStaffAdditionAllowed(tenantId, 4))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> adapter.validateStaffAdditionAllowed(tenantId, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum allowed staff members is 5");
    }
}
