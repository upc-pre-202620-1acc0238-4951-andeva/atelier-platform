package com.andeva.atelier.platform.crm.application;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.internal.commandservices.CustomerAffiliationServiceImpl;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory.PlatformAccount;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Affiliation of platform users as workshop customers (US44)")
class CustomerAffiliationServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMembershipRepository membershipRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CustomerCommandService customerCommandService;
    @Mock
    private PlatformAccountDirectory platformAccountDirectory;

    private CustomerAffiliationServiceImpl service;
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        service = new CustomerAffiliationServiceImpl(customerRepository, membershipRepository, vehicleRepository,
                customerCommandService, platformAccountDirectory);
    }

    @Test
    @DisplayName("Registers the account holder as customer and links the account to it")
    void shouldAffiliateExistingAccount() {
        UUID userId = UUID.randomUUID();
        when(platformAccountDirectory.findByEmail("ana@correo.pe"))
                .thenReturn(Optional.of(new PlatformAccount(userId, "ana@correo.pe", "Ana María Rojas")));
        Customer registered = Customer.registerIndividual(CustomerId.generate(), tenantId, PersonName.of("Ana", "María Rojas"),
                TaxId.of("41234567"), EmailAddress.of("ana@correo.pe"), null);
        when(customerCommandService.handle(any(RegisterIndividualCustomerCommand.class))).thenReturn(Result.success(registered));
        when(vehicleRepository.findByCurrentUserId(any())).thenReturn(List.of());

        var result = service.affiliate(tenantId, " Ana@Correo.pe ", "41234567", null);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<RegisterIndividualCustomerCommand> command = ArgumentCaptor.forClass(RegisterIndividualCustomerCommand.class);
        verify(customerCommandService).handle(command.capture());
        assertThat(command.getValue().firstName()).isEqualTo("Ana");
        assertThat(command.getValue().lastName()).isEqualTo("María Rojas");
        assertThat(command.getValue().email()).isEqualTo("ana@correo.pe");
        verify(membershipRepository).save(any(CustomerMembership.class));
    }

    @Test
    @DisplayName("Sends walk-ins to the full registration when no platform account exists")
    void shouldRejectWithoutPlatformAccount() {
        when(platformAccountDirectory.findByEmail("nuevo@correo.pe")).thenReturn(Optional.empty());

        var result = service.affiliate(tenantId, "nuevo@correo.pe", "41234567", null);

        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
        verify(customerCommandService, never()).handle(any(RegisterIndividualCustomerCommand.class));
    }
}
