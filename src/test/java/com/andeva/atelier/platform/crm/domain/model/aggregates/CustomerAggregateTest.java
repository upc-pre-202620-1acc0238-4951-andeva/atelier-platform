package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Customer Aggregate Unit Tests")
class CustomerAggregateTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @Nested
    @DisplayName("Individual Customer Invariants")
    class IndividualCustomerTests {

        @Test
        @DisplayName("Should successfully register individual customer with valid data")
        void shouldRegisterIndividualCustomer() {
            CustomerId id = CustomerId.generate();
            PersonName name = PersonName.of("Juan", "Perez");
            TaxId taxId = TaxId.of("12345678");
            EmailAddress email = EmailAddress.of("juan.perez@example.com");
            PhoneNumber phone = PhoneNumber.of("+51999888777");

            Customer customer = Customer.registerIndividual(id, tenantId, name, taxId, email, phone);

            assertThat(customer.id()).isEqualTo(id);
            assertThat(customer.tenantId()).isEqualTo(tenantId);
            assertThat(customer.type()).isEqualTo(CustomerType.INDIVIDUAL);
            assertThat(customer.name().getFullName()).isEqualTo("Juan Perez");
            assertThat(customer.getDisplayName()).isEqualTo("Juan Perez");
            assertThat(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.domainEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when individual customer has empty contact info")
        void shouldThrowWhenNoContactInfoProvided() {
            CustomerId id = CustomerId.generate();
            PersonName name = PersonName.of("Carlos", "Gomez");
            TaxId taxId = TaxId.of("87654321");

            assertThatThrownBy(() -> Customer.registerIndividual(id, tenantId, name, taxId, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one contact channel");
        }
    }

    @Nested
    @DisplayName("Company Customer Invariants")
    class CompanyCustomerTests {

        @Test
        @DisplayName("Should successfully register corporate company customer")
        void shouldRegisterCompanyCustomer() {
            CustomerId id = CustomerId.generate();
            TaxId taxId = TaxId.of("20131312955");
            EmailAddress email = EmailAddress.of("contacto@transportes.pe");
            PhoneNumber phone = PhoneNumber.of("+5114567890");

            Customer customer = Customer.registerCompany(id, tenantId, "Transportes Rápidos SAC", taxId, email, phone);

            assertThat(customer.type()).isEqualTo(CustomerType.COMPANY);
            assertThat(customer.companyName()).isEqualTo("Transportes Rápidos SAC");
            assertThat(customer.getDisplayName()).isEqualTo("Transportes Rápidos SAC");
            assertThat(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.domainEvents()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when company customer has blank company name")
        void shouldThrowWhenBlankCompanyName() {
            CustomerId id = CustomerId.generate();
            TaxId taxId = TaxId.of("20600123450");
            EmailAddress email = EmailAddress.of("info@corp.pe");

            assertThatThrownBy(() -> Customer.registerCompany(id, tenantId, "   ", taxId, email, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("State Transitions & Contact Updates")
    class StateAndContactTests {

        @Test
        @DisplayName("Should deactivate and reactivate customer successfully")
        void shouldTransitionStatus() {
            Customer customer = Customer.registerIndividual(
                    CustomerId.generate(),
                    tenantId,
                    PersonName.of("Ana", "Torres"),
                    TaxId.of("45678912"),
                    EmailAddress.of("ana@example.com"),
                    null
            );

            customer.deactivate();
            assertThat(customer.status()).isEqualTo(CustomerStatus.INACTIVE);

            customer.activate();
            assertThat(customer.status()).isEqualTo(CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should update contact channels successfully and record event")
        void shouldUpdateContactInfo() {
            Customer customer = Customer.registerIndividual(
                    CustomerId.generate(),
                    tenantId,
                    PersonName.of("Luis", "Rios"),
                    TaxId.of("78912345"),
                    EmailAddress.of("luis@old.com"),
                    null
            );

            customer.clearDomainEvents();
            customer.updateContact(EmailAddress.of("luis@new.com"), PhoneNumber.of("+51987654321"));

            assertThat(customer.email().value()).isEqualTo("luis@new.com");
            assertThat(customer.phone().value()).isEqualTo("+51987654321");
            assertThat(customer.domainEvents()).hasSize(1);
        }
    }
}
