package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Size;

/**
 * Request payload registering the Firebase Cloud Messaging token of the signed-in mobile device.
 * Sending a null or blank token unregisters the device (e.g. on sign-out).
 *
 * @param fcmToken FCM registration token issued by the Firebase SDK on the device
 * @author Joel Huamani Estefanero
 */
public record RegisterDeviceTokenResource(
        @Size(max = 4096)
        String fcmToken
) {
}
