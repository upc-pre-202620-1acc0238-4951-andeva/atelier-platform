package com.andeva.atelier.platform.crm.application.acl;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.AppointmentAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerMembershipAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Open Host Service (OHS) facade implementation for Customer and Fleet Management context.
 *
 * @author Adiel Sanchez Santin
 */
@Service
public class CustomerFleetContextFacadeImpl implements CustomerFleetContextFacade {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerMembershipRepository customerMembershipRepository;
    private final AppointmentCommandService appointmentCommandService;

    public CustomerFleetContextFacadeImpl(
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            AppointmentRepository appointmentRepository,
            CustomerMembershipRepository customerMembershipRepository,
            AppointmentCommandService appointmentCommandService
    ) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository cannot be null");
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "AppointmentRepository cannot be null");
        this.customerMembershipRepository = Objects.requireNonNull(customerMembershipRepository, "CustomerMembershipRepository cannot be null");
        this.appointmentCommandService = Objects.requireNonNull(appointmentCommandService, "AppointmentCommandService cannot be null");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerAclDto> fetchCustomerById(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        return customerRepository.findById(CustomerId.of(customerId))
                .map(this::toAclDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerAclDto> fetchCustomerByTenantIdAndTaxId(UUID tenantId, String taxId) {
        if (tenantId == null || taxId == null) {
            return Optional.empty();
        }
        return customerRepository.findByTenantIdAndTaxId(TenantId.of(tenantId), taxId)
                .map(this::toAclDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleAclDto> fetchVehicleById(UUID vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }
        return vehicleRepository.findById(VehicleId.of(vehicleId))
                .map(this::toAclDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleAclDto> fetchVehicleByPlate(String plate) {
        if (plate == null || plate.isBlank()) {
            return Optional.empty();
        }
        try {
            LicensePlate licensePlate = LicensePlate.of(plate);
            return vehicleRepository.findByPlate(licensePlate)
                    .map(this::toAclDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> fetchCurrentOwnerId(UUID vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }
        return vehicleRepository.findById(VehicleId.of(vehicleId))
                .flatMap(Vehicle::getActiveOwnership)
                .map(VehicleOwnership::getCustomerId)
                .map(CustomerId::value);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleAclDto> fetchVehiclesByCustomerId(UUID customerId) {
        if (customerId == null) {
            return List.of();
        }
        return vehicleRepository.findByCurrentOwnerId(CustomerId.of(customerId))
                .stream()
                .map(this::toAclDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppointmentAclDto> fetchAppointmentById(UUID appointmentId) {
        if (appointmentId == null) {
            return Optional.empty();
        }
        return appointmentRepository.findById(AppointmentId.of(appointmentId))
                .map(this::toAclDto);
    }

    @Override
    @Transactional
    public boolean markAppointmentAsConvertedToWorkOrder(UUID appointmentId) {
        if (appointmentId == null) {
            return false;
        }
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(AppointmentId.of(appointmentId));
        if (appointmentOpt.isEmpty()) {
            return false;
        }
        Appointment appointment = appointmentOpt.get();
        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                new MarkAppointmentArrivedCommand(appointment.tenantId(), appointment.id())
        );
        return result.isSuccess();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerMembershipAclDto> fetchActiveMembershipsByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return customerMembershipRepository.findByUserIdAndStatus(UserId.of(userId), CustomerMembershipStatus.ACTIVE)
                .stream()
                .map(this::toAclDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFleetRole(UUID customerId, UUID userId, String requiredRole) {
        if (customerId == null || userId == null || requiredRole == null) {
            return false;
        }
        Optional<CustomerMembership> membershipOpt =
                customerMembershipRepository.findByCustomerIdAndUserId(CustomerId.of(customerId), UserId.of(userId));

        if (membershipOpt.isEmpty() || membershipOpt.get().getStatus() != CustomerMembershipStatus.ACTIVE) {
            return false;
        }

        FleetRole userRole = membershipOpt.get().getRole();
        if (userRole == FleetRole.FLEET_ADMIN) {
            return true;
        }
        return userRole.name().equalsIgnoreCase(requiredRole);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerMembershipAclDto> fetchMembership(UUID customerId, UUID userId) {
        if (customerId == null || userId == null) {
            return Optional.empty();
        }
        return customerMembershipRepository.findByCustomerIdAndUserId(CustomerId.of(customerId), UserId.of(userId))
                .map(this::toAclDto);
    }

    @Override
    @Transactional
    public Optional<UUID> schedulePreventiveAppointment(UUID tenantId, UUID vehicleId, String description) {
        if (tenantId == null || vehicleId == null) {
            return Optional.empty();
        }
        Optional<UUID> ownerIdOpt = fetchCurrentOwnerId(vehicleId);
        if (ownerIdOpt.isEmpty()) {
            return Optional.empty();
        }
        try {
            ScheduleAppointmentCommand command = new ScheduleAppointmentCommand(
                    TenantId.of(tenantId),
                    BranchId.generate(),
                    CustomerId.of(ownerIdOpt.get()),
                    VehicleId.of(vehicleId),
                    Instant.now().plus(Duration.ofDays(1)),
                    60,
                    description != null ? description : "Preventative maintenance scheduled from IoT Telemetry"
            );
            Result<Appointment, ApplicationError> result = appointmentCommandService.handle(command);
            if (result.isSuccess() && result.toOptional().isPresent()) {
                return Optional.of(result.toOptional().get().id().value());
            }
        } catch (Exception e) {
            // Log and return empty
        }
        return Optional.empty();
    }

    private CustomerAclDto toAclDto(Customer customer) {
        return new CustomerAclDto(
                customer.id().value(),
                customer.tenantId().value(),
                customer.type().name(),
                customer.getDisplayName(),
                customer.taxId().value(),
                customer.email() != null ? customer.email().value() : null,
                customer.phone() != null ? customer.phone().value() : null,
                customer.status().name()
        );
    }

    private VehicleAclDto toAclDto(Vehicle vehicle) {
        UUID currentOwnerId = vehicle.getActiveOwnership()
                .map(VehicleOwnership::getCustomerId)
                .filter(Objects::nonNull)
                .map(CustomerId::value)
                .orElse(null);

        return new VehicleAclDto(
                vehicle.id().value(),
                vehicle.plate().value(),
                vehicle.vin() != null ? vehicle.vin().value() : null,
                vehicle.brand(),
                vehicle.model(),
                vehicle.year(),
                vehicle.engineType().name(),
                currentOwnerId
        );
    }

    private AppointmentAclDto toAclDto(Appointment appointment) {
        return new AppointmentAclDto(
                appointment.id().value(),
                appointment.tenantId().value(),
                appointment.branchId().value(),
                appointment.customerId().value(),
                appointment.vehicleId().value(),
                appointment.scheduledAt().toString(),
                appointment.estimatedDurationMinutes(),
                appointment.status().name()
        );
    }

    private CustomerMembershipAclDto toAclDto(CustomerMembership membership) {
        return new CustomerMembershipAclDto(
                membership.getId().value(),
                membership.getCustomerId().value(),
                membership.getUserId().value(),
                membership.getRole().name(),
                membership.getStatus().name()
        );
    }
}
