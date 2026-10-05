package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.DeactivateCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.UpdateCustomerContactCommand;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of CustomerCommandService handling customer write operations.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class CustomerCommandServiceImpl implements CustomerCommandService {

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final SubscriptionValidationService subscriptionValidationService;

    public CustomerCommandServiceImpl(
            CustomerRepository customerRepository,
            AppointmentRepository appointmentRepository,
            SubscriptionValidationService subscriptionValidationService
    ) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "AppointmentRepository cannot be null");
        this.subscriptionValidationService = Objects.requireNonNull(subscriptionValidationService, "SubscriptionValidationService cannot be null");
    }

    @Override
    public Result<Customer, ApplicationError> handle(RegisterIndividualCustomerCommand command) {
        Objects.requireNonNull(command, "RegisterIndividualCustomerCommand cannot be null");

        if (customerRepository.existsByTenantIdAndTaxId(command.tenantId(), command.taxId())) {
            return Result.failure(ApplicationError.conflict("Customer with tax ID " + command.taxId() + " already exists in this workshop"));
        }

        if (command.email() != null && !command.email().isBlank()
                && customerRepository.existsByTenantIdAndEmail(command.tenantId(), command.email())) {
            return Result.failure(ApplicationError.conflict("Customer with email " + command.email() + " already exists in this workshop"));
        }

        if (!subscriptionValidationService.validateCustomerQuota(command.tenantId().value())) {
            return Result.failure(ApplicationError.forbidden("Workshop customer quota exceeded for active subscription plan"));
        }

        PersonName name = PersonName.of(command.firstName(), command.lastName());
        TaxId taxId = TaxId.of(command.taxId());
        EmailAddress email = command.email() != null && !command.email().isBlank() ? EmailAddress.of(command.email()) : null;
        PhoneNumber phone = command.phone() != null && !command.phone().isBlank() ? PhoneNumber.of(command.phone()) : null;

        Customer customer = Customer.registerIndividual(
                CustomerId.generate(),
                command.tenantId(),
                name,
                taxId,
                email,
                phone
        );

        Customer saved = customerRepository.save(customer);
        return Result.success(saved);
    }

    @Override
    public Result<Customer, ApplicationError> handle(RegisterCompanyCustomerCommand command) {
        Objects.requireNonNull(command, "RegisterCompanyCustomerCommand cannot be null");

        if (customerRepository.existsByTenantIdAndTaxId(command.tenantId(), command.taxId())) {
            return Result.failure(ApplicationError.conflict("Company customer with tax ID " + command.taxId() + " already exists in this workshop"));
        }

        if (command.email() != null && !command.email().isBlank()
                && customerRepository.existsByTenantIdAndEmail(command.tenantId(), command.email())) {
            return Result.failure(ApplicationError.conflict("Company customer with email " + command.email() + " already exists in this workshop"));
        }

        if (!subscriptionValidationService.validateCustomerQuota(command.tenantId().value())) {
            return Result.failure(ApplicationError.forbidden("Workshop customer quota exceeded for active subscription plan"));
        }

        TaxId taxId = TaxId.of(command.taxId());
        EmailAddress email = command.email() != null && !command.email().isBlank() ? EmailAddress.of(command.email()) : null;
        PhoneNumber phone = command.phone() != null && !command.phone().isBlank() ? PhoneNumber.of(command.phone()) : null;

        Customer customer = Customer.registerCompany(
                CustomerId.generate(),
                command.tenantId(),
                command.companyName(),
                taxId,
                email,
                phone
        );

        Customer saved = customerRepository.save(customer);
        return Result.success(saved);
    }

    @Override
    public Result<Customer, ApplicationError> handle(UpdateCustomerContactCommand command) {
        Objects.requireNonNull(command, "UpdateCustomerContactCommand cannot be null");

        Optional<Customer> customerOpt = customerRepository.findByIdAndTenantId(command.customerId(), command.tenantId());
        if (customerOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Customer", command.customerId().value()));
        }

        Customer customer = customerOpt.get();
        EmailAddress email = command.email() != null && !command.email().isBlank() ? EmailAddress.of(command.email()) : null;
        PhoneNumber phone = command.phone() != null && !command.phone().isBlank() ? PhoneNumber.of(command.phone()) : null;

        try {
            customer.updateContact(email, phone);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Customer saved = customerRepository.save(customer);
        return Result.success(saved);
    }

    @Override
    public Result<Customer, ApplicationError> handle(DeactivateCustomerCommand command) {
        Objects.requireNonNull(command, "DeactivateCustomerCommand cannot be null");

        Optional<Customer> customerOpt = customerRepository.findByIdAndTenantId(command.customerId(), command.tenantId());
        if (customerOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Customer", command.customerId().value()));
        }

        if (appointmentRepository.existsActiveAppointmentsByCustomerId(command.customerId())) {
            return Result.failure(ApplicationError.conflict("Cannot deactivate customer with pending or confirmed appointments"));
        }

        Customer customer = customerOpt.get();
        customer.deactivate();
        Customer saved = customerRepository.save(customer);
        return Result.success(saved);
    }
}
