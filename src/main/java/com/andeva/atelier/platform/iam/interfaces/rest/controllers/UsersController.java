package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterDeviceTokenCommand;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.RegisterDeviceTokenResource;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST controller for self-service operations on the authenticated user account.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Self-service endpoints for the authenticated user account")
public class UsersController {

    private final UserCommandService userCommandService;

    public UsersController(UserCommandService userCommandService) {
        this.userCommandService = Objects.requireNonNull(userCommandService, "UserCommandService cannot be null");
    }

    /**
     * Registers the FCM token of the mobile device so push alerts can reach it.
     */
    @Operation(summary = "Register or clear the push notification (FCM) token of the current device")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device token registered"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid JWT")
    })
    @PutMapping("/me/device-token")
    public ResponseEntity<?> registerDeviceToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterDeviceTokenResource resource
    ) {
        if (userDetails == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to register a device token"));
        }

        RegisterDeviceTokenCommand command = new RegisterDeviceTokenCommand(UserId.of(userDetails.getUserId()), resource.fcmToken());
        return ResponseEntityAssembler.toResponseEntityFromResult(
                userCommandService.handle(command),
                unused -> null,
                HttpStatus.NO_CONTENT
        );
    }
}
