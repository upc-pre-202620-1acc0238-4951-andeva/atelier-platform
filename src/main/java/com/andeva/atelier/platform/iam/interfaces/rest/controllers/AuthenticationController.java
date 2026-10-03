package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.VerifyEmailTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateTenantResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.ForgotPasswordResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.GoogleSignInResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.ResetPasswordResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.SignInResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.VerifyEmailResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.AuthenticatedUserResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.MessageResponseResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantSummaryResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.CreateTenantCommandFromResourceAssembler;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.TenantResourceFromAggregateAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST controller exposing endpoints for workshop onboarding, tenant signup,
 * local and federated user authentication, OTP email verification, and password reset flows.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping({"/api/v1/auth", "/api/v1/authentication"})
@Tag(name = "Authentication & Onboarding", description = "Endpoints for user sign-in, tenant registration, password reset, and email verification")
public class AuthenticationController {

    private final TenantCommandService tenantCommandService;
    private final UserCommandService userCommandService;
    private final TenantQueryService tenantQueryService;
    private final CreateTenantCommandFromResourceAssembler createTenantCommandAssembler;
    private final BCryptHashingService hashingService;

    public AuthenticationController(
            TenantCommandService tenantCommandService,
            UserCommandService userCommandService,
            TenantQueryService tenantQueryService,
            CreateTenantCommandFromResourceAssembler createTenantCommandAssembler,
            BCryptHashingService hashingService
    ) {
        this.tenantCommandService = Objects.requireNonNull(tenantCommandService, "TenantCommandService cannot be null");
        this.userCommandService = Objects.requireNonNull(userCommandService, "UserCommandService cannot be null");
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
        this.createTenantCommandAssembler = Objects.requireNonNull(createTenantCommandAssembler, "CreateTenantCommandFromResourceAssembler cannot be null");
        this.hashingService = Objects.requireNonNull(hashingService, "BCryptHashingService cannot be null");
    }

    /**
     * Registers a new automotive workshop Tenant, clones factory RBAC roles,
     * and provisions the initial administrative user account.
     */
    @Operation(summary = "Register and provision a new workshop Tenant with initial administrator")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tenant and administrator successfully provisioned"),
            @ApiResponse(responseCode = "400", description = "Validation constraint violations in submitted payload"),
            @ApiResponse(responseCode = "409", description = "Tax ID (RUC) or admin email already registered in system")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody CreateTenantResource resource) {
        CreateTenantCommand command = createTenantCommandAssembler.toCommandFromResource(resource);
        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                TenantResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.CREATED
        );
    }

    /**
     * Authenticates a user using local email and password credentials, returning an enriched JWT Bearer token.
     */
    @Operation(summary = "Authenticate user with local email and password credentials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully, JWT issued"),
            @ApiResponse(responseCode = "400", description = "Malformed credentials or missing parameters"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified/suspended account")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
        AuthenticateUserCommand command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toAuthenticatedUserResource,
                HttpStatus.OK
        );
    }

    /**
     * Authenticates a user using Google OAuth2 OpenID Connect ID token.
     */
    @Operation(summary = "Authenticate user using Google ID token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated via Google successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed Google ID token"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired Google ID token")
    })
    @PostMapping("/google-sign-in")
    public ResponseEntity<?> googleSignIn(@Valid @RequestBody GoogleSignInResource resource) {
        AuthenticateWithGoogleCommand command = new AuthenticateWithGoogleCommand(resource.idToken());
        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toAuthenticatedUserResource,
                HttpStatus.OK
        );
    }

    /**
     * Confirms user account activation via one-time verification token.
     */
    @Operation(summary = "Confirm email verification using one-time token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully verified and account activated"),
            @ApiResponse(responseCode = "400", description = "Invalid, expired, or previously consumed token")
    })
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailResource resource) {
        VerifyEmailTokenCommand command = resource.email() != null && !resource.email().isBlank()
                ? new VerifyEmailTokenCommand(EmailAddress.of(resource.email()), resource.token())
                : new VerifyEmailTokenCommand(resource.token());
        Result<Void, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                unused -> new MessageResponseResource("Email successfully verified"),
                HttpStatus.OK
        );
    }

    /**
     * Dispatches a secure password recovery token link via Resend HTTPS API.
     */
    @Operation(summary = "Request password reset token via email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset link dispatched if email exists"),
            @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        RequestPasswordResetCommand command = new RequestPasswordResetCommand(EmailAddress.of(resource.email()));
        Result<Void, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                unused -> new MessageResponseResource("If the email exists, a password reset link has been dispatched"),
                HttpStatus.OK
        );
    }

    /**
     * Consumes password reset token and applies newly chosen password hash.
     */
    @Operation(summary = "Confirm password reset and set new password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Invalid, expired, or already used reset token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        String hashedPassword = hashingService.hash(resource.newPassword());
        ResetPasswordCommand command = new ResetPasswordCommand(resource.token(), Password.of(hashedPassword));
        Result<Void, ApplicationError> result = userCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                unused -> new MessageResponseResource("Password successfully reset"),
                HttpStatus.OK
        );
    }

    private AuthenticatedUserResource toAuthenticatedUserResource(AuthenticatedUser authUser) {
        TenantSummaryResource tenantSummary = null;
        if (authUser.tenantId() != null) {
            tenantSummary = tenantQueryService.handle(new GetTenantByIdQuery(authUser.tenantId()))
                    .map(TenantResourceFromAggregateAssembler::toSummaryFromAggregate)
                    .orElse(new TenantSummaryResource(authUser.tenantId().value(), "", ""));
        }

        String fullName = (authUser.user().profile() != null)
                ? authUser.user().profile().getFullName()
                : "";

        return new AuthenticatedUserResource(
                authUser.user().id().value(),
                authUser.user().email().value(),
                fullName,
                authUser.token(),
                "Bearer",
                tenantSummary,
                authUser.permissions().stream().sorted().toList()
        );
    }
}
