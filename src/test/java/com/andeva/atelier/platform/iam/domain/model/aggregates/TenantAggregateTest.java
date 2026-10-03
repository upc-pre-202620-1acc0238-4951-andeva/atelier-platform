package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.events.BranchCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantActivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantRegisteredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantSuspendedEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link Tenant} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Tenant Aggregate Root Unit Tests")
class TenantAggregateTest {

    private final TaxId ruc = TaxId.of("20456789014");

    @Test
    @DisplayName("Should create Tenant, initialize ACTIVE status and register TenantRegisteredEvent")
    void shouldCreateTenantAndRegisterEvent() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);

        assertThat(tenant.id()).isNotNull();
        assertThat(tenant.name()).isEqualTo("AutoTaller Pro");
        assertThat(tenant.legalName()).isEqualTo("AutoTaller Pro S.A.C.");
        assertThat(tenant.taxId()).isEqualTo(ruc);
        assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.branches()).isEmpty();

        assertThat(tenant.domainEvents()).hasSize(1);
        assertThat(tenant.domainEvents().iterator().next())
                .isInstanceOf(TenantRegisteredEvent.class);
    }

    @Test
    @DisplayName("Should assign Stripe customer ID")
    void shouldAssignStripeCustomerId() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);
        tenant.assignStripeCustomerId("cus_stripe_123456");

        assertThat(tenant.stripeCustomerId()).isEqualTo("cus_stripe_123456");
    }

    @Test
    @DisplayName("Should manage tenant activation and suspension lifecycle with events")
    void shouldManageActivationAndSuspension() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);
        tenant.clearDomainEvents();

        tenant.suspend("Non-payment of subscription fee");
        assertThat(tenant.status()).isEqualTo(TenantStatus.SUSPENDED);
        assertThat(tenant.domainEvents()).hasSize(1);
        assertThat(tenant.domainEvents().iterator().next())
                .isInstanceOf(TenantSuspendedEvent.class);

        // Cannot suspend if already suspended
        assertThatThrownBy(() -> tenant.suspend("Duplicate suspension"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already suspended");

        tenant.clearDomainEvents();
        tenant.activate();
        assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.domainEvents()).hasSize(1);
        assertThat(tenant.domainEvents().iterator().next())
                .isInstanceOf(TenantActivatedEvent.class);
    }

    @Test
    @DisplayName("Should add branch, register BranchCreatedEvent, and locate branch by ID")
    void shouldAddAndFindBranch() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);
        tenant.clearDomainEvents();

        GeoPoint coords = GeoPoint.of(-12.1121, -77.0145);
        Branch branch = tenant.addBranch("Sede Central", "0001", coords, 200);

        assertThat(tenant.branches()).hasSize(1);
        assertThat(tenant.branches().get(0)).isEqualTo(branch);

        assertThat(tenant.domainEvents()).hasSize(1);
        assertThat(tenant.domainEvents().iterator().next())
                .isInstanceOf(BranchCreatedEvent.class);

        Optional<Branch> found = tenant.findBranchById(branch.id());
        assertThat(found).isPresent().contains(branch);

        Optional<Branch> missing = tenant.findBranchById(BranchId.generate());
        assertThat(missing).isEmpty();
    }

    @Test
    @DisplayName("Should update profile metadata and enforce string limits")
    void shouldUpdateProfileAndEnforceLimits() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);

        tenant.updateProfile("AutoTaller Premium", "AutoTaller Premium Perú S.A.C.");
        assertThat(tenant.name()).isEqualTo("AutoTaller Premium");
        assertThat(tenant.legalName()).isEqualTo("AutoTaller Premium Perú S.A.C.");

        assertThatThrownBy(() -> tenant.updateProfile(" ", "Legal"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> tenant.updateProfile("Name", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Branches collection must be unmodifiable from outside the aggregate")
    void branchesCollectionMustBeUnmodifiable() {
        Tenant tenant = Tenant.create("AutoTaller Pro", "AutoTaller Pro S.A.C.", ruc);
        Branch branch = tenant.addBranch("Sede Central", "0001", GeoPoint.of(0, 0), 100);

        assertThatThrownBy(() -> tenant.branches().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
