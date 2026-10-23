package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

/**
 * Plan of the workshop sent with the session: the staff app shows or hides the IoT and AI modules with it.
 *
 * @param tier          GO, PRO, MAX, ENTERPRISE (NONE without a subscription)
 * @param status        TRIALING, ACTIVE, PAST_DUE, CANCELED… (INACTIVE without a subscription)
 * @param accessGranted subscription active, in trial or within its grace period
 */
public record SessionPlanResource(
        String planName,
        String tier,
        String status,
        boolean accessGranted,
        boolean iotTelemetryEnabled,
        boolean aiDiagnosticsEnabled
) {
}
