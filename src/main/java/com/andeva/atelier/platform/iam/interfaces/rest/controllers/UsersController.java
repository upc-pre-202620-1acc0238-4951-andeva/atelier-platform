package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.UpdateProfileResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.AuthenticatedUserResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantSummaryResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.UserProfileResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.TenantResourceFromAggregateAssembler;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.UserProfileResourceFromEntityAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller providing user self-service account profile and demographic inspection.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Profile", description = "Endpoints for authenticated user self-service and profile management")
public class UsersController {

    private final UserQueryService userQueryService;
    private final TenantQueryService tenantQueryService;
    private final UserCommandService userCommandService;

    public UsersController(
            UserQueryService userQueryService,
            TenantQueryService tenantQueryService,
            UserCommandService userCommandService
    ) {
        this.userQueryService = Objects.requireNonNull(userQueryService, "UserQueryService cannot be null");
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
        this.userCommandService = Objects.requireNonNull(userCommandService, "UserCommandService cannot be null");
    }

    /**
     * Retrieves session profile, active tenant summary, and granted permissions for the authenticated caller.
     */
    @Operation(summary = "Get current authenticated user profile and permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile and authorities retrieved"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User account not found")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUserId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to view current user profile"));
        }

        Optional<User> userOptional = userQueryService.handle(new GetUserByIdQuery(UserId.of(userDetails.getUserId())));
        if (userOptional.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("User account not found in system"));
        }

        User user = userOptional.get();

        TenantSummaryResource tenantSummary = null;
        if (userDetails.getTenantId() != null) {
            tenantSummary = tenantQueryService.handle(new GetTenantByIdQuery(new TenantId(userDetails.getTenantId())))
                    .map(TenantResourceFromAggregateAssembler::toSummaryFromAggregate)
                    .orElse(new TenantSummaryResource(userDetails.getTenantId(), "", ""));
        }

        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        AuthenticatedUserResource resource = new AuthenticatedUserResource(
                user.id().value(),
                user.email().value(),
                user.profile() != null ? user.profile().getFullName() : "",
                "",
                "Bearer",
                tenantSummary,
                permissions
        );

        return ResponseEntity.ok(resource);
    }

    /**
     * Updates personal demographic names and mobile telephone contact details of the authenticated caller.
     */
    @Operation(summary = "Update personal profile information of current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation constraint failure on names or phone"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User account not found")
    })
    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileResource resource
    ) {
        if (userDetails == null || userDetails.getUserId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to update profile"));
        }

        PhoneNumber phone = (resource.phone() != null && !resource.phone().isBlank())
                ? PhoneNumber.of(resource.phone())
                : null;

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                UserId.of(userDetails.getUserId()),
                PersonName.of(resource.firstName(), resource.lastName()),
                phone
        );
        Result<User, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserProfileResourceFromEntityAssembler::toResourceFromUser,
                HttpStatus.OK
        );
    }
}
