package com.andeva.atelier.platform.iam.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

/**
 * Outbound Anti-Corruption Layer port for transactional email delivery via Resend API.
 *
 * @author Joel Huamani Estefanero
 */
public interface ResendEmailService {

    /**
     * Sends an email verification message containing a 6-digit numeric OTP code.
     *
     * @param recipient the target user email address
     * @param otpCode the 6-digit verification code
     */
    void sendVerificationEmail(EmailAddress recipient, String otpCode);

    /**
     * Sends a password reset email with a secure redemption URL.
     *
     * @param recipient the target user email address
     * @param resetToken the cryptographic reset token
     */
    void sendPasswordResetEmail(EmailAddress recipient, String resetToken);

    /**
     * Sends a staff onboarding invitation email with tenant details and join link.
     *
     * @param recipient the target employee email address
     * @param tenantName the legal or commercial name of the workshop tenant
     * @param inviteToken the cryptographic invitation token
     */
    void sendStaffInvitationEmail(EmailAddress recipient, String tenantName, String inviteToken);
}
