package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.model.events.SupplierRegisteredEvent;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Supplier Aggregate Domain Tests")
class SupplierTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final String businessName = "Distribuidora Automotriz del Centro S.A.C.";
    private final TaxId taxId = TaxId.of("20556677881");
    private final String contactName = "Carlos Mendoza";
    private final String phone = "+51987654321";
    private final String email = "ventas@distribuidoracentro.pe";
    private final String address = "Av. Nicolás Arriola 1250, La Victoria, Lima";

    @Test
    @DisplayName("Should register new supplier in ACTIVE status and emit SupplierRegisteredEvent")
    void shouldRegisterSupplier() {
        Supplier supplier = Supplier.register(
                tenantId,
                businessName,
                taxId,
                contactName,
                phone,
                email,
                address
        );

        assertThat(supplier.getId()).isNotNull();
        assertThat(supplier.getTenantId()).isEqualTo(tenantId);
        assertThat(supplier.getBusinessName()).isEqualTo(businessName);
        assertThat(supplier.getTaxId()).isEqualTo(taxId);
        assertThat(supplier.getContactName()).contains(contactName);
        assertThat(supplier.getPhone()).contains(phone);
        assertThat(supplier.getEmail()).contains(email);
        assertThat(supplier.getAddress()).contains(address);
        assertThat(supplier.isActive()).isTrue();

        assertThat(supplier.domainEvents()).anyMatch(e -> e instanceof SupplierRegisteredEvent);
    }

    @Test
    @DisplayName("Should update supplier contact details and address")
    void shouldUpdateSupplierContact() {
        Supplier supplier = Supplier.register(
                tenantId,
                businessName,
                taxId,
                contactName,
                phone,
                email,
                address
        );

        supplier.updateContact("Juan Perez", "+51999888777", "jperez@distribuidoracentro.pe", "Av. Grau 500");

        assertThat(supplier.getContactName()).contains("Juan Perez");
        assertThat(supplier.getPhone()).contains("+51999888777");
        assertThat(supplier.getEmail()).contains("jperez@distribuidoracentro.pe");
        assertThat(supplier.getAddress()).contains("Av. Grau 500");
    }

    @Test
    @DisplayName("Should deactivate supplier")
    void shouldDeactivateSupplier() {
        Supplier supplier = Supplier.register(
                tenantId,
                businessName,
                taxId,
                contactName,
                phone,
                email,
                address
        );

        supplier.deactivate();
        assertThat(supplier.isActive()).isFalse();
    }
}
