package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.InviteCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RevokeCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying {@link CustomerMembershipCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerMembershipCommandServiceImpl Unit Tests")
class CustomerMembershipCommandServiceTest {

    @Mock
    private CustomerMembershipRepository customerMembershipRepository;

    @Mock
    private CustomerRepository customerRepository;

    private CustomerMembershipCommandServiceImpl service;

    private TenantId sampleTenantId;
    private CustomerId sampleCustomerId;
    private UserId sampleUserId;

    @BeforeEach
    void setUp() {
        service = new CustomerMembershipCommandServiceImpl(
                customerMembershipRepository,
                customerRepository
        );
        sampleTenantId = TenantId.generate();
        sampleCustomerId = CustomerId.generate();
        sampleUserId = UserId.generate();
    }

    @Test
    @DisplayName("Should invite member successfully when customer is a COMPANY")
    void shouldInviteMemberSuccessfully() {
        Customer companyCustomer = Customer.registerCompany(
                sampleCustomerId,
                sampleTenantId,
                "Flota Logística SAC",
                TaxId.of("20100070970"),
                EmailAddress.of("contacto@flotalogistica.pe"),
                PhoneNumber.of("+51987654321")
        );

        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(companyCustomer));
        when(customerMembershipRepository.existsByCustomerIdAndUserIdAndStatus(sampleCustomerId, sampleUserId, CustomerMembershipStatus.ACTIVE))
                .thenReturn(false);
        when(customerMembershipRepository.save(any(CustomerMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteCustomerMemberCommand command = new InviteCustomerMemberCommand(
                sampleTenantId,
                sampleCustomerId,
                sampleUserId,
                FleetRole.FLEET_ADMIN
        );

        Result<CustomerMembership, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        CustomerMembership saved = result.toOptional().orElseThrow();
        assertThat(saved.getRole()).isEqualTo(FleetRole.FLEET_ADMIN);
        assertThat(saved.getStatus()).isEqualTo(CustomerMembershipStatus.ACTIVE);
        verify(customerMembershipRepository).save(any(CustomerMembership.class));
    }

    @Test
    @DisplayName("Should reject inviting member when customer is an INDIVIDUAL (not a company)")
    void shouldRejectWhenCustomerIsNotCompany() {
        Customer individualCustomer = Customer.registerIndividual(
                sampleCustomerId,
                sampleTenantId,
                PersonName.of("Carlos", "Rios"),
                TaxId.of("45871234"),
                EmailAddress.of("carlos.rios@example.pe"),
                PhoneNumber.of("+51987654321")
        );

        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(individualCustomer));

        InviteCustomerMemberCommand command = new InviteCustomerMemberCommand(
                sampleTenantId,
                sampleCustomerId,
                sampleUserId,
                FleetRole.FLEET_OPERATOR
        );

        Result<CustomerMembership, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("corporate company");
    }

    @Test
    @DisplayName("Should reject inviting member when active membership already exists")
    void shouldRejectWhenMembershipAlreadyExists() {
        Customer companyCustomer = Customer.registerCompany(
                sampleCustomerId,
                sampleTenantId,
                "Flota Logística SAC",
                TaxId.of("20100070970"),
                EmailAddress.of("contacto@flotalogistica.pe"),
                PhoneNumber.of("+51987654321")
        );

        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(companyCustomer));
        when(customerMembershipRepository.existsByCustomerIdAndUserIdAndStatus(sampleCustomerId, sampleUserId, CustomerMembershipStatus.ACTIVE))
                .thenReturn(true);

        InviteCustomerMemberCommand command = new InviteCustomerMemberCommand(
                sampleTenantId,
                sampleCustomerId,
                sampleUserId,
                FleetRole.FLEET_ADMIN
        );

        Result<CustomerMembership, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("already holds an active membership");
    }

    @Test
    @DisplayName("Should revoke member successfully")
    void shouldRevokeMemberSuccessfully() {
        CustomerMembership membership = CustomerMembership.create(
                CustomerMembershipId.generate(),
                sampleCustomerId,
                sampleUserId,
                FleetRole.FLEET_OPERATOR
        );

        when(customerMembershipRepository.findByCustomerIdAndUserId(sampleCustomerId, sampleUserId))
                .thenReturn(Optional.of(membership));
        when(customerMembershipRepository.save(membership)).thenReturn(membership);

        RevokeCustomerMemberCommand command = new RevokeCustomerMemberCommand(
                sampleTenantId,
                sampleCustomerId,
                sampleUserId
        );

        Result<Void, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(membership.getStatus()).isEqualTo(CustomerMembershipStatus.REVOKED);
    }
}
