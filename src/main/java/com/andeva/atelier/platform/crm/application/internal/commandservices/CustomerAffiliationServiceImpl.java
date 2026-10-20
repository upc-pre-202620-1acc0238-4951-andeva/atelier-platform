package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerAffiliationService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory.PlatformAccount;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CustomerAffiliationServiceImpl implements CustomerAffiliationService {

    private final CustomerRepository customerRepository;
    private final CustomerMembershipRepository membershipRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerCommandService customerCommandService;
    private final PlatformAccountDirectory platformAccountDirectory;

    public CustomerAffiliationServiceImpl(
            CustomerRepository customerRepository,
            CustomerMembershipRepository membershipRepository,
            VehicleRepository vehicleRepository,
            CustomerCommandService customerCommandService,
            PlatformAccountDirectory platformAccountDirectory
    ) {
        this.customerRepository = Objects.requireNonNull(customerRepository);
        this.membershipRepository = Objects.requireNonNull(membershipRepository);
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
        this.customerCommandService = Objects.requireNonNull(customerCommandService);
        this.platformAccountDirectory = Objects.requireNonNull(platformAccountDirectory);
    }

    @Override
    @Transactional(readOnly = true)
    public AffiliationLookup lookup(TenantId tenantId, String email) {
        String normalized = normalize(email);
        Optional<Customer> customer = customerRepository.findByTenantIdAndEmail(tenantId, normalized);
        Optional<PlatformAccount> account = platformAccountDirectory.findByEmail(normalized);
        List<Vehicle> vehicles = account.map(a -> vehicleRepository.findByCurrentUserId(UserId.of(a.userId()))).orElse(List.of());
        return new AffiliationLookup(customer, account, vehicles);
    }

    @Override
    public Result<Customer, ApplicationError> affiliate(TenantId tenantId, String email, String taxId, String phone) {
        String normalized = normalize(email);
        Optional<Customer> existing = customerRepository.findByTenantIdAndEmail(tenantId, normalized);
        if (existing.isPresent()) {
            return Result.failure(ApplicationError.conflict("The person is already a customer of this workshop: " + existing.get().id().value()));
        }
        Optional<PlatformAccount> account = platformAccountDirectory.findByEmail(normalized);
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No platform account uses this email: register the customer as a walk-in"));
        }

        String[] names = splitName(account.get().fullName());
        Result<Customer, ApplicationError> registered = customerCommandService.handle(
                new RegisterIndividualCustomerCommand(tenantId, names[0], names[1], taxId, normalized, phone));
        if (registered.isFailure()) {
            return registered;
        }
        Customer customer = registered.getOrThrow();
        UserId userId = UserId.of(account.get().userId());

        membershipRepository.save(CustomerMembership.create(CustomerMembershipId.generate(), customer.id(), userId, FleetRole.FLEET_ADMIN));

        // The vehicles the person registered in the app become the customer's units in this workshop
        for (Vehicle vehicle : vehicleRepository.findByCurrentUserId(userId)) {
            if (vehicle.getCurrentOwnerId().isEmpty()) {
                vehicle.linkCustomer(customer.id());
                vehicleRepository.save(vehicle);
            }
        }
        return Result.success(customer);
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String[] splitName(String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        int space = trimmed.indexOf(' ');
        if (space < 0) {
            return new String[]{trimmed.isEmpty() ? "Cliente" : trimmed, "-"};
        }
        return new String[]{trimmed.substring(0, space), trimmed.substring(space + 1).trim()};
    }
}
