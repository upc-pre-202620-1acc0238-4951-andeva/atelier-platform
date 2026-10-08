package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByTaxIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomersByTenantIdQuery;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateCompanyCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateIndividualCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.UpdateCustomerContactResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.CustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.CustomerResourceFromAggregateAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.RegisterCustomerCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.UpdateCustomerContactCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.domain.model.commands.DeactivateCustomerCommand;
import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.VehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.VehicleResourceFromAggregateAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing commercial customer accounts and portfolios.
 * Canonical specification from 03-crm-and-fleet.md Section 5.3.1.
 *
 * @author Adiel Sanchez Santin
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Endpoints for managing workshop customer profiles and commercial relationships")
public class CustomersController {

    private final CustomerCommandService customerCommandService;
    private final CustomerQueryService customerQueryService;
    private final VehicleQueryService vehicleQueryService;

    public CustomersController(
            CustomerCommandService customerCommandService,
            CustomerQueryService customerQueryService,
            VehicleQueryService vehicleQueryService
    ) {
        this.customerCommandService = Objects.requireNonNull(customerCommandService, "CustomerCommandService cannot be null");
        this.customerQueryService = Objects.requireNonNull(customerQueryService, "CustomerQueryService cannot be null");
        this.vehicleQueryService = Objects.requireNonNull(vehicleQueryService, "VehicleQueryService cannot be null");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('crm:customers:read')")
    @Operation(summary = "List and search customer portfolio of active workshop")
    public ResponseEntity<?> getCustomers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to access customer portfolio"));
        }

        CustomerType customerType = type != null && !type.isBlank()
                ? CustomerType.valueOf(type.trim().toUpperCase(Locale.ROOT))
                : null;

        CustomerStatus customerStatus = status != null && !status.isBlank()
                ? CustomerStatus.valueOf(status.trim().toUpperCase(Locale.ROOT))
                : null;

        List<Customer> customers = customerQueryService.handle(new GetCustomersByTenantIdQuery(
                TenantId.of(userDetails.getTenantId()),
                customerType,
                search,
                customerStatus
        ));

        List<CustomerResource> resources = customers.stream()
                .map(CustomerResourceFromAggregateAssembler::toResourceFromEntity)
                .toList();

        if (size <= 0) {
            size = 20;
        }
        int fromIndex = Math.min(Math.max(0, page) * size, resources.size());
        int toIndex = Math.min(fromIndex + size, resources.size());
        List<CustomerResource> paged = resources.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paged);
    }

    @PostMapping("/individuals")
    @PreAuthorize("hasAuthority('crm:customers:create')")
    @Operation(summary = "Register a natural person (individual) customer")
    public ResponseEntity<?> registerIndividualCustomer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateIndividualCustomerResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to register customer"));
        }

        Result<Customer, ApplicationError> result = customerCommandService.handle(
                RegisterCustomerCommandFromResourceAssembler.toCommandFromResource(userDetails.getTenantId(), resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CustomerResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/companies")
    @PreAuthorize("hasAuthority('crm:customers:create')")
    @Operation(summary = "Register a corporate company customer")
    public ResponseEntity<?> registerCompanyCustomer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCompanyCustomerResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to register company customer"));
        }

        Result<Customer, ApplicationError> result = customerCommandService.handle(
                RegisterCustomerCommandFromResourceAssembler.toCommandFromResource(userDetails.getTenantId(), resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CustomerResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:customers:read')")
    @Operation(summary = "Retrieve customer profile by identifier")
    public ResponseEntity<?> getCustomerById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Optional<Customer> customerOpt = customerQueryService.handle(
                new GetCustomerByIdQuery(TenantId.of(userDetails.getTenantId()), CustomerId.of(id)));

        if (customerOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Customer", id));
        }

        return ResponseEntity.ok(CustomerResourceFromAggregateAssembler.toResourceFromEntity(customerOpt.get()));
    }

    @PutMapping("/{id}/contact")
    @PreAuthorize("hasAuthority('crm:customers:update')")
    @Operation(summary = "Update customer contact phone and email")
    public ResponseEntity<?> updateContact(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerContactResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Customer, ApplicationError> result = customerCommandService.handle(
                UpdateCustomerContactCommandFromResourceAssembler.toCommandFromResource(
                        userDetails.getTenantId(), id, resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CustomerResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}/vehicles")
    @PreAuthorize("hasAuthority('crm:customers:read')")
    @Operation(summary = "List vehicles currently under active ownership of a customer")
    public ResponseEntity<?> getCustomerVehicles(@PathVariable UUID id) {
        List<Vehicle> vehicles = vehicleQueryService.handle(
                new GetVehiclesByCustomerIdQuery(CustomerId.of(id)));

        List<VehicleResource> resources = vehicles.stream()
                .map(VehicleResourceFromAggregateAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }
}
