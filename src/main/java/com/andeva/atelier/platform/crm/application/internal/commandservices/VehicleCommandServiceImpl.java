package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.TransferVehicleOwnershipCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.domain.services.VehicleTransferDomainService;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of VehicleCommandService handling vehicle lifecycle write operations.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionValidationService subscriptionValidationService;
    private final VehicleTransferDomainService vehicleTransferDomainService;

    public VehicleCommandServiceImpl(
            VehicleRepository vehicleRepository,
            CustomerRepository customerRepository,
            SubscriptionValidationService subscriptionValidationService,
            VehicleTransferDomainService vehicleTransferDomainService
    ) {
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository cannot be null");
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
        this.subscriptionValidationService = Objects.requireNonNull(subscriptionValidationService, "SubscriptionValidationService cannot be null");
        this.vehicleTransferDomainService = Objects.requireNonNull(vehicleTransferDomainService, "VehicleTransferDomainService cannot be null");
    }

    @Override
    public Result<Vehicle, ApplicationError> handle(RegisterVehicleCommand command) {
        Objects.requireNonNull(command, "RegisterVehicleCommand cannot be null");

        LicensePlate plate;
        try {
            plate = LicensePlate.of(command.plate());
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        if (vehicleRepository.existsByPlate(plate)) {
            return Result.failure(ApplicationError.conflict("Vehicle with license plate " + plate.value() + " already exists in platform"));
        }

        if (command.vin() != null && !command.vin().isBlank()) {
            if (vehicleRepository.existsByVin(command.vin())) {
                return Result.failure(ApplicationError.conflict("Vehicle with VIN " + command.vin() + " already exists in platform"));
            }
        }

        if (command.customerId().isEmpty() && command.userId().isEmpty()) {
            return Result.failure(ApplicationError.badRequest("Vehicle must be registered with an initial customer or user owner"));
        }

        if (command.customerId().isPresent()) {
            Optional<Customer> customerOpt = customerRepository.findByIdAndTenantId(command.customerId().get(), command.tenantId());
            if (customerOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Customer", command.customerId().get().value()));
            }
        }

        if (!subscriptionValidationService.validateVehicleQuota(command.tenantId().value())) {
            return Result.failure(ApplicationError.forbidden("Workshop vehicle quota exceeded for active subscription plan"));
        }

        Vin vin = command.vin() != null && !command.vin().isBlank() ? Vin.of(command.vin()) : null;

        Vehicle vehicle = Vehicle.register(
                VehicleId.generate(),
                plate,
                vin,
                command.brand(),
                command.model(),
                command.year(),
                command.engineType(),
                command.customerId(),
                command.userId()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return Result.success(saved);
    }

    @Override
    public Result<Vehicle, ApplicationError> handle(TransferVehicleOwnershipCommand command) {
        Objects.requireNonNull(command, "TransferVehicleOwnershipCommand cannot be null");

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(command.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Vehicle", command.vehicleId().value()));
        }

        Optional<Customer> targetCustomerOpt = customerRepository.findByIdAndTenantId(command.newOwnerId(), command.tenantId());
        if (targetCustomerOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Customer", command.newOwnerId().value()));
        }

        Vehicle vehicle = vehicleOpt.get();
        Customer targetCustomer = targetCustomerOpt.get();

        Result<VehicleOwnership, DomainException> transferResult =
                vehicleTransferDomainService.transferVehicle(vehicle, targetCustomer, command.transferDate());

        if (transferResult instanceof Result.Failure<VehicleOwnership, DomainException> failure) {
            return Result.failure(ApplicationError.badRequest(failure.error().getMessage()));
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        return Result.success(saved);
    }
}
