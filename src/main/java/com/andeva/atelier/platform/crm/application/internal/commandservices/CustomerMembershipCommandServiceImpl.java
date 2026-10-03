package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerMembershipCommandService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.InviteCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RevokeCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of CustomerMembershipCommandService handling corporate fleet membership write operations.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class CustomerMembershipCommandServiceImpl implements CustomerMembershipCommandService {

    private final CustomerMembershipRepository customerMembershipRepository;
    private final CustomerRepository customerRepository;

    public CustomerMembershipCommandServiceImpl(
            CustomerMembershipRepository customerMembershipRepository,
            CustomerRepository customerRepository
    ) {
        this.customerMembershipRepository = Objects.requireNonNull(customerMembershipRepository, "CustomerMembershipRepository cannot be null");
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
    }

    @Override
    public Result<CustomerMembership, ApplicationError> handle(InviteCustomerMemberCommand command) {
        Objects.requireNonNull(command, "InviteCustomerMemberCommand cannot be null");

        Optional<Customer> customerOpt = customerRepository.findByIdAndTenantId(command.customerId(), command.tenantId());
        if (customerOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Customer", command.customerId().value()));
        }

        Customer customer = customerOpt.get();
        if (customer.type() != CustomerType.COMPANY) {
            return Result.failure(ApplicationError.badRequest("Fleet memberships are only supported for corporate company customers"));
        }

        if (customerMembershipRepository.existsByCustomerIdAndUserIdAndStatus(command.customerId(), command.userId(), CustomerMembershipStatus.ACTIVE)) {
            return Result.failure(ApplicationError.conflict("User already holds an active membership for this customer"));
        }

        CustomerMembership membership = CustomerMembership.create(
                CustomerMembershipId.generate(),
                command.customerId(),
                command.userId(),
                command.role()
        );

        CustomerMembership saved = customerMembershipRepository.save(membership);
        return Result.success(saved);
    }

    @Override
    public Result<Void, ApplicationError> handle(RevokeCustomerMemberCommand command) {
        Objects.requireNonNull(command, "RevokeCustomerMemberCommand cannot be null");

        Optional<CustomerMembership> membershipOpt =
                customerMembershipRepository.findByCustomerIdAndUserId(command.customerId(), command.userId());
        if (membershipOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("CustomerMembership", command.userId().value()));
        }

        CustomerMembership membership = membershipOpt.get();
        membership.revoke();
        customerMembershipRepository.save(membership);
        return Result.success(null);
    }
}
