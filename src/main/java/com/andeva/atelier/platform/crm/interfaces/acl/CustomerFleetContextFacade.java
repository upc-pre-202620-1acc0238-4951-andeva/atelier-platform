package com.andeva.atelier.platform.crm.interfaces.acl;

import com.andeva.atelier.platform.crm.interfaces.acl.dto.AppointmentAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerMembershipAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Open Host Service (OHS) / Inbound ACL Facade for Customer and Fleet Management (CRM).
 * Exposes synchronous in-memory read operations and controlled transactional commands
 * for client modules without exposing domain aggregates or sharing JPA entities.
 *
 * @author Adiel Sanchez Santin
 */
public interface CustomerFleetContextFacade {

    /**
     * Query a customer by unique identifier.
     *
     * @param customerId Customer universal identifier
     * @return Immutable customer DTO if found
     */
    Optional<CustomerAclDto> fetchCustomerById(UUID customerId);

    /**
     * Query a customer by tenant and tax identifier (DNI or RUC).
     *
     * @param tenantId Tenant workshop identifier
     * @param taxId    Tax identification number
     * @return Immutable customer DTO if found
     */
    Optional<CustomerAclDto> fetchCustomerByTenantIdAndTaxId(UUID tenantId, String taxId);

    /**
     * Query technical specifications of a vehicle by unique identifier.
     *
     * @param vehicleId Vehicle universal identifier
     * @return Immutable vehicle DTO if found
     */
    Optional<VehicleAclDto> fetchVehicleById(UUID vehicleId);

    /**
     * Query a vehicle by its normalized license plate.
     *
     * @param plate Normalized license plate string
     * @return Immutable vehicle DTO if found
     */
    Optional<VehicleAclDto> fetchVehicleByPlate(String plate);

    /**
     * Query the active owner customer identifier for a vehicle.
     *
     * @param vehicleId Vehicle universal identifier
     * @return Current owner customer ID if active
     */
    Optional<UUID> fetchCurrentOwnerId(UUID vehicleId);

    /**
     * List all vehicles currently under active ownership of a customer.
     *
     * @param customerId Customer identifier
     * @return List of active vehicle DTOs
     */
    List<VehicleAclDto> fetchVehiclesByCustomerId(UUID customerId);

    /**
     * Query metadata for a scheduled appointment.
     *
     * @param appointmentId Appointment identifier
     * @return Immutable appointment DTO if found
     */
    Optional<AppointmentAclDto> fetchAppointmentById(UUID appointmentId);

    /**
     * Mark an appointment as arrived and converted to a work order.
     *
     * @param appointmentId Appointment identifier
     * @return true if state transition succeeded; false otherwise
     */
    boolean markAppointmentAsConvertedToWorkOrder(UUID appointmentId);

    /**
     * Query active corporate fleet memberships assigned to a user.
     *
     * @param userId User identifier
     * @return List of active membership DTOs
     */
    List<CustomerMembershipAclDto> fetchActiveMembershipsByUserId(UUID userId);

    /**
     * Verify whether a user holds a specific corporate role in a fleet.
     *
     * @param customerId   Corporate customer identifier
     * @param userId       User identifier
     * @param requiredRole Required role (FLEET_ADMIN or FLEET_OPERATOR)
     * @return true if the user holds an active membership with the required role; false otherwise
     */
    boolean hasFleetRole(UUID customerId, UUID userId, String requiredRole);

    /**
     * Query corporate fleet membership between a customer and a user.
     *
     * @param customerId Corporate customer identifier
     * @param userId     User identifier
     * @return Membership DTO if present
     */
    Optional<CustomerMembershipAclDto> fetchMembership(UUID customerId, UUID userId);

    /**
     * Schedules a preventative workshop appointment for a vehicle initiated from predictive alerts.
     *
     * @param tenantId    Tenant workshop identifier
     * @param vehicleId   Target vehicle identifier
     * @param description Contextual reason for scheduling
     * @return Optional appointment ID if created successfully
     */
    Optional<UUID> schedulePreventiveAppointment(UUID tenantId, UUID vehicleId, String description);
}
