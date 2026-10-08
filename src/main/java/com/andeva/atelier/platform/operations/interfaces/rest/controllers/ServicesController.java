package com.andeva.atelier.platform.operations.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.application.queryservices.ServiceQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.andeva.atelier.platform.operations.domain.model.queries.GetServicesByTenantIdQuery;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateServiceResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.UpdateServiceResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.ServiceResource;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.CreateServiceCommandFromResourceAssembler;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.ServiceResourceAssembler;
import com.andeva.atelier.platform.operations.interfaces.rest.transform.UpdateServiceCommandFromResourceAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller for managing workshop service catalog, labor tariffs, and standard durations.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Workshop Services", description = "Endpoints for managing workshop service catalog, labor tariffs, and standard durations")
public class ServicesController {

    private final ServiceCommandService serviceCommandService;
    private final ServiceQueryService serviceQueryService;

    public ServicesController(ServiceCommandService serviceCommandService, ServiceQueryService serviceQueryService) {
        this.serviceCommandService = Objects.requireNonNull(serviceCommandService, "serviceCommandService cannot be null");
        this.serviceQueryService = Objects.requireNonNull(serviceQueryService, "serviceQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('operations:services:write') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Register standard catalog service in workshop")
    public ResponseEntity<?> createService(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateServiceResource resource,
            UriComponentsBuilder ucb
    ) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();

        CreateServiceItemCommand command = CreateServiceCommandFromResourceAssembler.toCommandFromResource(tenantId, resource);
        Result<Service, ApplicationError> result = serviceCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        Service created = result.getOrThrow();
        ServiceResource responseResource = ServiceResourceAssembler.toResourceFromEntity(created);
        URI location = ucb.path("/api/v1/services/{id}").buildAndExpand(created.getId().value()).toUri();
        return ResponseEntity.created(location).body(responseResource);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('operations:services:read') or isAuthenticated()")
    @Operation(summary = "List all standard catalog services for tenant")
    public ResponseEntity<?> getServices(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID tenantId = userDetails != null && userDetails.getTenantId() != null
                ? userDetails.getTenantId()
                : UUID.randomUUID();

        List<Service> services = serviceQueryService.handle(new GetServicesByTenantIdQuery(new TenantId(tenantId)));
        List<ServiceResource> resources = services.stream()
                .map(ServiceResourceAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('operations:services:read') or isAuthenticated()")
    @Operation(summary = "Get standard service catalog item by id")
    public ResponseEntity<?> getServiceById(@PathVariable UUID serviceId) {
        return serviceQueryService.handle(new GetServiceByIdQuery(new ServiceId(serviceId)))
                .map(ServiceResourceAssembler::toResourceFromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasAuthority('operations:services:write') or hasRole('TENANT_ADMIN')")
    @Operation(summary = "Update standard service catalog item")
    public ResponseEntity<?> updateService(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateServiceResource resource
    ) {
        UpdateServiceItemCommand command = UpdateServiceCommandFromResourceAssembler.toCommandFromResource(serviceId, resource);
        Result<Service, ApplicationError> result = serviceCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(ServiceResourceAssembler.toResourceFromEntity(result.getOrThrow()));
    }
}
