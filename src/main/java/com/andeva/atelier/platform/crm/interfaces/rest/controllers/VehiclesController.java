package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByPlateQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleOwnershipHistoryQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateVehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.TransferVehicleOwnershipResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.VehicleOwnershipResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.VehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.RegisterVehicleCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.TransferVehicleOwnershipCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.VehicleOwnershipResourceFromEntityAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.VehicleResourceFromAggregateAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing universal vehicles and ownership transfer history.
 * Canonical specification from 03-crm-and-fleet.md Section 5.3.1.
 *
 * @author Adiel Sanchez Santin
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Endpoints for managing universal vehicle registry and legal custody transfers")
public class VehiclesController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehiclesController(
            VehicleCommandService vehicleCommandService,
            VehicleQueryService vehicleQueryService
    ) {
        this.vehicleCommandService = Objects.requireNonNull(vehicleCommandService, "VehicleCommandService cannot be null");
        this.vehicleQueryService = Objects.requireNonNull(vehicleQueryService, "VehicleQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:vehicles:create')")
    @Operation(summary = "Register universal vehicle with initial customer or driver ownership")
    public ResponseEntity<?> registerVehicle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateVehicleResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to register vehicle"));
        }

        Result<Vehicle, ApplicationError> result = vehicleCommandService.handle(
                RegisterVehicleCommandFromResourceAssembler.toCommandFromResource(userDetails.getTenantId(), resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                VehicleResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:vehicles:read')")
    @Operation(summary = "Retrieve vehicle specifications by identifier")
    public ResponseEntity<?> getVehicleById(@PathVariable UUID id) {
        Optional<Vehicle> vehicleOpt = vehicleQueryService.handle(new GetVehicleByIdQuery(VehicleId.of(id)));
        if (vehicleOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Vehicle", id));
        }

        return ResponseEntity.ok(VehicleResourceFromAggregateAssembler.toResourceFromEntity(vehicleOpt.get()));
    }

    @GetMapping("/by-plate/{plate}")
    @PreAuthorize("hasAuthority('crm:vehicles:read')")
    @Operation(summary = "Retrieve vehicle by normalized license plate")
    public ResponseEntity<?> getVehicleByPlate(@PathVariable String plate) {
        LicensePlate licensePlate;
        try {
            licensePlate = LicensePlate.of(plate);
        } catch (IllegalArgumentException e) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.badRequest(e.getMessage()));
        }

        Optional<Vehicle> vehicleOpt = vehicleQueryService.handle(new GetVehicleByPlateQuery(licensePlate));
        if (vehicleOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Vehicle with plate " + plate + " not found"));
        }

        return ResponseEntity.ok(VehicleResourceFromAggregateAssembler.toResourceFromEntity(vehicleOpt.get()));
    }

    @PostMapping("/{id}/ownerships")
    @PreAuthorize("hasAuthority('crm:vehicles:update')")
    @Operation(summary = "Transfer vehicle ownership creating a new custody period")
    public ResponseEntity<?> transferOwnership(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody TransferVehicleOwnershipResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to transfer ownership"));
        }

        Result<Vehicle, ApplicationError> result = vehicleCommandService.handle(
                TransferVehicleOwnershipCommandFromResourceAssembler.toCommandFromResource(
                        userDetails.getTenantId(), id, resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                VehicleResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}/ownerships")
    @PreAuthorize("hasAuthority('crm:vehicles:read')")
    @Operation(summary = "Retrieve chronological ownership transfer history of a vehicle")
    public ResponseEntity<?> getOwnershipHistory(@PathVariable UUID id) {
        List<VehicleOwnership> history = vehicleQueryService.handle(
                new GetVehicleOwnershipHistoryQuery(VehicleId.of(id)));

        List<VehicleOwnershipResource> resources = history.stream()
                .map(VehicleOwnershipResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/my-vehicles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve vehicles registered to or actively operated by authenticated user")
    public ResponseEntity<?> getMyVehicles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        List<Vehicle> vehicles = vehicleQueryService.handle(
                new com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByUserIdQuery(
                        UserId.of(userDetails.getUserId())));

        List<VehicleResource> resources = vehicles.stream()
                .map(VehicleResourceFromAggregateAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }
}
