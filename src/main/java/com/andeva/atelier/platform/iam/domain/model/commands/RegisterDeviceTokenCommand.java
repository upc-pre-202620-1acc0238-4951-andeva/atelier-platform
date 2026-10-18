package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Command registering (or clearing, when blank) the Firebase Cloud Messaging token
 * of the mobile device the user is currently signed in on.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterDeviceTokenCommand(
        UserId userId,
        String fcmToken
) {
    public RegisterDeviceTokenCommand {
        Objects.requireNonNull(userId, "User identifier cannot be null");
        fcmToken = fcmToken != null && !fcmToken.isBlank() ? fcmToken.trim() : null;
    }
}
