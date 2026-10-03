package com.andeva.atelier.platform.iam.infrastructure.security.hashing;

import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Infrastructure implementation of {@link BCryptHashingService} wrapping Spring Security's
 * {@link BCryptPasswordEncoder} with work factor 12.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class BCryptHashingServiceImpl implements BCryptHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptHashingServiceImpl() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    public BCryptHashingServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "Password encoder cannot be null");
    }

    @Override
    public String hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "Raw password cannot be null");
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedHash) {
        if (rawPassword == null || encodedHash == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedHash);
    }
}
