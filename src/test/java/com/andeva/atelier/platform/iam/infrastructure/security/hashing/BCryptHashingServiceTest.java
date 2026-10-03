package com.andeva.atelier.platform.iam.infrastructure.security.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link BCryptHashingServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
class BCryptHashingServiceTest {

    private final BCryptHashingServiceImpl hashingService = new BCryptHashingServiceImpl();

    @Test
    @DisplayName("Should successfully hash password and verify matches")
    void testHashAndMatch() {
        String rawPassword = "AdminSecretPassword123!";
        String hash = hashingService.hash(rawPassword);

        assertThat(hash).isNotNull();
        assertThat(hash).startsWith("$2a$12$");
        assertThat(hashingService.matches(rawPassword, hash)).isTrue();
        assertThat(hashingService.matches("WrongPassword", hash)).isFalse();
    }

    @Test
    @DisplayName("Should reject null raw password on hashing")
    void testNullHash() {
        assertThatThrownBy(() -> hashingService.hash(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should return false on null parameters for matches")
    void testNullMatches() {
        assertThat(hashingService.matches(null, "$2a$12$...hash")).isFalse();
        assertThat(hashingService.matches("password", null)).isFalse();
    }
}
