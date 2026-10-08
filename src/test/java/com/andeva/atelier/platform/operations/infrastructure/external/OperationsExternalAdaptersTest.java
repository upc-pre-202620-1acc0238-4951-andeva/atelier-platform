package com.andeva.atelier.platform.operations.infrastructure.external;

import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.UserAclDto;
import com.andeva.atelier.platform.inventory.interfaces.acl.InventoryContextFacade;
import com.andeva.atelier.platform.inventory.interfaces.acl.dto.PartSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryItemSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.MechanicStaffDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.VehicleSummaryDto;
import com.andeva.atelier.platform.operations.infrastructure.external.acl.crm.CustomerFleetAclAdapter;
import com.andeva.atelier.platform.operations.infrastructure.external.acl.iam.TenancyAclAdapter;
import com.andeva.atelier.platform.operations.infrastructure.external.acl.inventory.InventoryReservationAclAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Operations outbound Anti-Corruption Layer adapters.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Operations Outbound ACL Adapters Unit Tests")
class OperationsExternalAdaptersTest {

    @Nested
    @DisplayName("CustomerFleetAclAdapter Tests")
    class CustomerFleetAclAdapterTests {

        @Mock
        private CustomerFleetContextFacade crmFacade;

        @Test
        @DisplayName("Should validate vehicle when owner matches tenant")
        void shouldValidateVehicleWhenOwnerMatchesTenant() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID vehicleId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            VehicleAclDto vehicle = new VehicleAclDto(vehicleId, "ABC-123", "VIN123", "Toyota", "Corolla", 2022, "GASOLINE", ownerId);
            CustomerAclDto customer = new CustomerAclDto(ownerId, tenantId, "INDIVIDUAL", "John Doe", "12345678", "john@atelier.com", "+51999999999", "ACTIVE");

            when(crmFacade.fetchVehicleById(vehicleId)).thenReturn(Optional.of(vehicle));
            when(crmFacade.fetchCustomerById(ownerId)).thenReturn(Optional.of(customer));

            boolean result = adapter.isVehicleValidForTenant(vehicleId, tenantId);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject vehicle when owner belongs to another tenant")
        void shouldRejectVehicleWhenOwnerBelongsToAnotherTenant() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID vehicleId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();
            UUID otherTenantId = UUID.randomUUID();
            UUID ownerId = UUID.randomUUID();

            VehicleAclDto vehicle = new VehicleAclDto(vehicleId, "ABC-123", "VIN123", "Toyota", "Corolla", 2022, "GASOLINE", ownerId);
            CustomerAclDto customer = new CustomerAclDto(ownerId, otherTenantId, "INDIVIDUAL", "John Doe", "12345678", "john@atelier.com", "+51999999999", "ACTIVE");

            when(crmFacade.fetchVehicleById(vehicleId)).thenReturn(Optional.of(vehicle));
            when(crmFacade.fetchCustomerById(ownerId)).thenReturn(Optional.of(customer));

            boolean result = adapter.isVehicleValidForTenant(vehicleId, tenantId);
            assertThat(result).isFalse();
        }


        @Test
        @DisplayName("Should reject vehicle when vehicle has no owner")
        void shouldRejectVehicleWithoutOwner() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID vehicleId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            VehicleAclDto vehicle = new VehicleAclDto(vehicleId, "ABC-123", "VIN123", "Toyota", "Corolla", 2022, "GASOLINE", null);
            when(crmFacade.fetchVehicleById(vehicleId)).thenReturn(Optional.of(vehicle));

            boolean result = adapter.isVehicleValidForTenant(vehicleId, tenantId);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject customer when tenant ID does not match")
        void shouldRejectCustomerWithMismatchingTenant() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID customerId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();
            UUID otherTenantId = UUID.randomUUID();

            CustomerAclDto customer = new CustomerAclDto(customerId, otherTenantId, "INDIVIDUAL", "Jane Doe", "87654321", "jane@atelier.com", "+51988888888", "ACTIVE");
            when(crmFacade.fetchCustomerById(customerId)).thenReturn(Optional.of(customer));

            boolean result = adapter.isCustomerValid(customerId, tenantId);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should validate active customer of tenant")
        void shouldValidateActiveCustomerOfTenant() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID customerId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CustomerAclDto customer = new CustomerAclDto(customerId, tenantId, "INDIVIDUAL", "Jane Doe", "87654321", "jane@atelier.com", "+51988888888", "ACTIVE");
            when(crmFacade.fetchCustomerById(customerId)).thenReturn(Optional.of(customer));

            boolean result = adapter.isCustomerValid(customerId, tenantId);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject inactive customer")
        void shouldRejectInactiveCustomer() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID customerId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            CustomerAclDto customer = new CustomerAclDto(customerId, tenantId, "INDIVIDUAL", "Jane Doe", "87654321", "jane@atelier.com", "+51988888888", "SUSPENDED");
            when(crmFacade.fetchCustomerById(customerId)).thenReturn(Optional.of(customer));

            boolean result = adapter.isCustomerValid(customerId, tenantId);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should fetch vehicle and customer details mapping correctly")
        void shouldFetchVehicleAndCustomerDetails() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter(crmFacade);
            UUID vehicleId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            VehicleAclDto vehicle = new VehicleAclDto(vehicleId, "XYZ-789", "VIN999", "Nissan", "Sentra", 2021, "GASOLINE", customerId);
            CustomerAclDto customer = new CustomerAclDto(customerId, tenantId, "COMPANY", "Transportes SAC", "20123456789", "fleet@trans.pe", "+51977777777", "ACTIVE");

            when(crmFacade.fetchVehicleById(vehicleId)).thenReturn(Optional.of(vehicle));
            when(crmFacade.fetchCustomerById(customerId)).thenReturn(Optional.of(customer));

            Optional<VehicleSummaryDto> vSummary = adapter.fetchVehicleDetails(vehicleId);
            assertThat(vSummary).isPresent();
            assertThat(vSummary.get().plate()).isEqualTo("XYZ-789");
            assertThat(vSummary.get().brand()).isEqualTo("Nissan");

            Optional<CustomerSummaryDto> cSummary = adapter.fetchCustomerDetails(customerId);
            assertThat(cSummary).isPresent();
            assertThat(cSummary.get().fullName()).isEqualTo("Transportes SAC");
            assertThat(cSummary.get().taxId()).isEqualTo("20123456789");
        }

        @Test
        @DisplayName("Should handle fallback when CRM facade is null")
        void shouldHandleFallbackWhenCrmFacadeIsNull() {
            CustomerFleetAclAdapter adapter = new CustomerFleetAclAdapter();
            UUID vehicleId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            assertThat(adapter.isVehicleValidForTenant(vehicleId, tenantId)).isTrue();
            assertThat(adapter.isCustomerValid(vehicleId, tenantId)).isTrue();
            assertThat(adapter.fetchVehicleDetails(vehicleId)).isPresent();
            assertThat(adapter.fetchCustomerDetails(vehicleId)).isPresent();
            assertThat(adapter.isVehicleValidForTenant(null, tenantId)).isFalse();
        }
    }

    @Nested
    @DisplayName("TenancyAclAdapter Tests")
    class TenancyAclAdapterTests {

        @Mock
        private TenancyContextFacade iamFacade;

        @Test
        @DisplayName("Should validate active branch of tenant")
        void shouldValidateActiveBranchOfTenant() {
            TenancyAclAdapter adapter = new TenancyAclAdapter(iamFacade);
            UUID branchId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            BranchAclDto branch = new BranchAclDto(branchId, tenantId, "Sede Central", "B001", true);
            when(iamFacade.fetchBranchById(branchId)).thenReturn(Optional.of(branch));

            boolean result = adapter.isBranchActive(branchId, tenantId);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject inactive branch or branch belonging to other tenant")
        void shouldRejectInactiveOrMismatchedBranch() {
            TenancyAclAdapter adapter = new TenancyAclAdapter(iamFacade);
            UUID branchId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();
            UUID otherTenantId = UUID.randomUUID();

            BranchAclDto branchInactive = new BranchAclDto(branchId, tenantId, "Sede Central", "B001", false);
            when(iamFacade.fetchBranchById(branchId)).thenReturn(Optional.of(branchInactive));
            assertThat(adapter.isBranchActive(branchId, tenantId)).isFalse();

            BranchAclDto branchOther = new BranchAclDto(branchId, otherTenantId, "Sede Norte", "B002", true);
            when(iamFacade.fetchBranchById(branchId)).thenReturn(Optional.of(branchOther));
            assertThat(adapter.isBranchActive(branchId, tenantId)).isFalse();
        }

        @Test
        @DisplayName("Should validate mechanic eligibility via tenant staff membership")
        void shouldValidateMechanicEligibility() {
            TenancyAclAdapter adapter = new TenancyAclAdapter(iamFacade);
            UUID mechanicId = UUID.randomUUID();
            UUID tenantId = UUID.randomUUID();

            when(iamFacade.isUserStaffMemberOfTenant(mechanicId, tenantId)).thenReturn(true);
            assertThat(adapter.isMechanicEligible(mechanicId, tenantId)).isTrue();

            when(iamFacade.isUserStaffMemberOfTenant(mechanicId, tenantId)).thenReturn(false);
            assertThat(adapter.isMechanicEligible(mechanicId, tenantId)).isFalse();
        }

        @Test
        @DisplayName("Should fetch mechanic details from IAM user DTO")
        void shouldFetchMechanicDetails() {
            TenancyAclAdapter adapter = new TenancyAclAdapter(iamFacade);
            UUID userId = UUID.randomUUID();

            UserAclDto user = new UserAclDto(userId, "mechanic@atelier.pe", "Carlos Perez", "ACTIVE");
            when(iamFacade.fetchUserById(userId)).thenReturn(Optional.of(user));

            Optional<MechanicStaffDto> result = adapter.fetchMechanicDetails(userId);
            assertThat(result).isPresent();
            assertThat(result.get().fullName()).isEqualTo("Carlos Perez");
            assertThat(result.get().email()).isEqualTo("mechanic@atelier.pe");
            assertThat(result.get().active()).isTrue();
        }

        @Test
        @DisplayName("Should handle fallback when IAM facade is null")
        void shouldHandleFallbackWhenIamFacadeIsNull() {
            TenancyAclAdapter adapter = new TenancyAclAdapter();
            UUID id = UUID.randomUUID();

            assertThat(adapter.isBranchActive(id, id)).isTrue();
            assertThat(adapter.isMechanicEligible(id, id)).isTrue();
            assertThat(adapter.fetchMechanicDetails(id)).isPresent();
            assertThat(adapter.isBranchActive(null, id)).isFalse();
        }
    }

    @Nested
    @DisplayName("InventoryReservationAclAdapter Tests")
    class InventoryReservationAclAdapterTests {

        @Mock
        private InventoryContextFacade inventoryFacade;

        @Test
        @DisplayName("Should check stock availability and fetch item details")
        void shouldCheckStockAvailabilityAndFetchDetails() {
            InventoryReservationAclAdapter adapter = new InventoryReservationAclAdapter(inventoryFacade);
            UUID tenantId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            when(inventoryFacade.hasAvailableStock(tenantId, itemId, new BigDecimal("4.00"))).thenReturn(true);
            when(inventoryFacade.hasAvailableStock(tenantId, itemId, new BigDecimal("100.00"))).thenReturn(false);

            assertThat(adapter.checkItemStockAvailability(tenantId, itemId, new BigDecimal("4.00"))).isTrue();
            assertThat(adapter.checkItemStockAvailability(tenantId, itemId, new BigDecimal("100.00"))).isFalse();

            PartSummaryDto part = new PartSummaryDto(itemId, "Filtro de Aceite Mann", "FLT-MANN-01", "FILTERS", new BigDecimal("45.00"), new BigDecimal("12.00"), "ACTIVE");
            when(inventoryFacade.getPartSummary(tenantId, itemId)).thenReturn(Optional.of(part));

            Optional<InventoryItemSummaryDto> details = adapter.fetchItemDetails(tenantId, itemId);
            assertThat(details).isPresent();
            assertThat(details.get().sku()).isEqualTo("FLT-MANN-01");
            assertThat(details.get().name()).isEqualTo("Filtro de Aceite Mann");
            assertThat(details.get().currentStock()).isEqualByComparingTo(new BigDecimal("12.00"));
        }


        @Test
        @DisplayName("Should resolve tenant via fallback when single-arg methods used")
        void shouldResolveTenantViaFallback() {
            InventoryReservationAclAdapter adapter = new InventoryReservationAclAdapter(inventoryFacade);
            UUID fallbackTenant = UUID.randomUUID();
            adapter.setResolvedTenantId(fallbackTenant);
            assertThat(adapter.getResolvedTenantId()).isEqualTo(fallbackTenant);

            UUID itemId = UUID.randomUUID();
            when(inventoryFacade.hasAvailableStock(fallbackTenant, itemId, new BigDecimal("5.00"))).thenReturn(true);
            assertThat(adapter.checkItemStockAvailability(itemId, new BigDecimal("5.00"))).isTrue();

            PartSummaryDto part = new PartSummaryDto(itemId, "Bujia NGK", "NGK-IR-01", "IGNITION", new BigDecimal("30.00"), new BigDecimal("20.00"), "ACTIVE");
            when(inventoryFacade.getPartSummary(fallbackTenant, itemId)).thenReturn(Optional.of(part));

            Optional<InventoryItemSummaryDto> details = adapter.fetchItemDetails(itemId);
            assertThat(details).isPresent();
            assertThat(details.get().sku()).isEqualTo("NGK-IR-01");
        }

        @Test
        @DisplayName("Should handle fallback when inventory facade is null")
        void shouldHandleFallbackWhenInventoryFacadeIsNull() {
            InventoryReservationAclAdapter adapter = new InventoryReservationAclAdapter();
            UUID tenantId = UUID.randomUUID();
            UUID itemId = UUID.randomUUID();

            assertThat(adapter.checkItemStockAvailability(tenantId, itemId, new BigDecimal("2.00"))).isTrue();
            assertThat(adapter.checkItemStockAvailability(tenantId, itemId, BigDecimal.ZERO)).isFalse();
            assertThat(adapter.fetchItemDetails(tenantId, itemId)).isPresent();
            assertThat(adapter.fetchItemDetails(tenantId, null)).isEmpty();
        }
    }
}
