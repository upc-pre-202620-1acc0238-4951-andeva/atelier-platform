package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.DeactivateCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.UpdateCustomerContactCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerCommandService Unit Tests")
class CustomerCommandServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private SubscriptionValidationService subscriptionValidationService;

    private CustomerCommandServiceImpl customerCommandService;
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        customerCommandService = new CustomerCommandServiceImpl(
                customerRepository,
                appointmentRepository,
                subscriptionValidationService
        );
    }

    @Test
    @DisplayName("Should successfully register individual customer when validations pass")
    void shouldRegisterIndividualCustomer() {
        RegisterIndividualCustomerCommand command = new RegisterIndividualCustomerCommand(
                tenantId,
                "Pedro",
                "Suarez",
                "10203040",
                "pedro@example.com",
                "+51987654321"
        );

        when(customerRepository.existsByTenantIdAndTaxId(tenantId, "10203040")).thenReturn(false);
        when(customerRepository.existsByTenantIdAndEmail(tenantId, "pedro@example.com")).thenReturn(false);
        when(subscriptionValidationService.validateCustomerQuota(tenantId.value())).thenReturn(true);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Customer, ApplicationError> result = customerCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Customer saved = ((Result.Success<Customer, ApplicationError>) result).value();
        assertThat(saved.name().getFullName()).isEqualTo("Pedro Suarez");
        assertThat(saved.type()).isEqualTo(CustomerType.INDIVIDUAL);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should return conflict when customer with taxId already exists")
    void shouldReturnConflictWhenTaxIdExists() {
        RegisterIndividualCustomerCommand command = new RegisterIndividualCustomerCommand(
                tenantId,
                "Pedro",
                "Suarez",
                "10203040",
                "pedro@example.com",
                null
        );

        when(customerRepository.existsByTenantIdAndTaxId(tenantId, "10203040")).thenReturn(true);

        Result<Customer, ApplicationError> result = customerCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        ApplicationError error = ((Result.Failure<Customer, ApplicationError>) result).error();
        assertThat(error.code()).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("Should update contact channels successfully")
    void shouldUpdateContact() {
        CustomerId customerId = CustomerId.generate();
        Customer customer = Customer.registerCompany(
                customerId,
                tenantId,
                "Trans Logistics",
                TaxId.of("20131312955"),
                EmailAddress.of("old@trans.pe"),
                PhoneNumber.of("+511234567")
        );

        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCustomerContactCommand command = new UpdateCustomerContactCommand(
                tenantId,
                customerId,
                "new@trans.pe",
                "+51987654321"
        );

        Result<Customer, ApplicationError> result = customerCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Customer updated = ((Result.Success<Customer, ApplicationError>) result).value();
        assertThat(updated.email().value()).isEqualTo("new@trans.pe");
    }

    @Test
    @DisplayName("Should refuse deactivation when active appointments exist")
    void shouldRefuseDeactivationWhenAppointmentsExist() {
        CustomerId customerId = CustomerId.generate();
        Customer customer = Customer.registerIndividual(
                customerId,
                tenantId,
                com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName.of("Mario", "Vargas"),
                TaxId.of("11223344"),
                EmailAddress.of("mario@example.com"),
                null
        );

        when(customerRepository.findByIdAndTenantId(customerId, tenantId)).thenReturn(Optional.of(customer));
        when(appointmentRepository.existsActiveAppointmentsByCustomerId(customerId)).thenReturn(true);

        DeactivateCustomerCommand command = new DeactivateCustomerCommand(tenantId, customerId);
        Result<Customer, ApplicationError> result = customerCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        ApplicationError error = ((Result.Failure<Customer, ApplicationError>) result).error();
        assertThat(error.code()).isEqualTo("CONFLICT");
    }
}
