package com.andeva.atelier.platform.iam.application.internal.eventhandlers;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.ResendEmailService;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.events.PasswordResetRequestedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.VerificationTokenIssuedEvent;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Application event listener orchestrating user communications upon domain event occurrences.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class UserDomainEventsHandler {

    private static final Logger log = LoggerFactory.getLogger(UserDomainEventsHandler.class);

    private final ResendEmailService resendEmailService;
    private final UserRepository userRepository;

    public UserDomainEventsHandler(ResendEmailService resendEmailService, UserRepository userRepository) {
        this.resendEmailService = Objects.requireNonNull(resendEmailService, "ResendEmailService cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    @EventListener
    @Async
    public void on(VerificationTokenIssuedEvent event) {
        log.info("Processing VerificationTokenIssuedEvent for user: {}", event.userId());
        if (event.type() == TokenType.EMAIL_VERIFICATION) {
            userRepository.findById(event.userId()).ifPresent(user -> {
                log.info("Sending email verification OTP to: {}", user.email().value());
                resendEmailService.sendVerificationEmail(user.email(), event.tokenValue());
            });
        }
    }

    @EventListener
    @Async
    public void on(PasswordResetRequestedEvent event) {
        log.info("Processing PasswordResetRequestedEvent for email: {}", event.email().value());
        resendEmailService.sendPasswordResetEmail(event.email(), event.tokenValue());
    }
}
