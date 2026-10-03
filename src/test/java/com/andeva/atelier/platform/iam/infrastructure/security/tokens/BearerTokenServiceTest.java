package com.andeva.atelier.platform.iam.infrastructure.security.tokens;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BearerTokenServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
class BearerTokenServiceTest {

    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String BCRYPT_HASH = "$2a$12$e80yq9gZ9uGfF8yq47y.I.vM4.a4V7vX5gD.0N0p4m4J6P2u1r2uS";

    private final BearerTokenServiceImpl tokenService = new BearerTokenServiceImpl(SECRET_KEY, 3600000L);

    @Test
    @DisplayName("Should generate, validate and extract claims from a valid JWT Bearer token")
    void testTokenLifecycle() {
        User user = User.registerWithLocalCredentials(
                EmailAddress.of("technician@workshop.pe"),
                Password.of(BCRYPT_HASH),
                PersonName.of("Jorge", "Benitez"),
                PhoneNumber.of("+51999888777")
        );
        TenantId tenantId = TenantId.generate();
        List<String> permissions = List.of("mro:work-orders:read", "mro:work-orders:update");

        String token = tokenService.generateToken(user, tenantId, permissions);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(tokenService.validateToken(token)).isTrue();

        Map<String, Object> claims = tokenService.extractClaims(token);
        assertThat(claims).isNotEmpty();
        assertThat(claims.get("sub")).isEqualTo(user.id().value().toString());
        assertThat(claims.get("email")).isEqualTo("technician@workshop.pe");
        assertThat(claims.get("tenantId")).isEqualTo(tenantId.value().toString());

        @SuppressWarnings("unchecked")
        List<String> extractedPerms = (List<String>) claims.get("permissions");
        assertThat(extractedPerms).containsExactlyInAnyOrderElementsOf(permissions);
    }

    @Test
    @DisplayName("Should return false on invalid or tampered JWT token")
    void testInvalidToken() {
        assertThat(tokenService.validateToken("invalid.jwt.token")).isFalse();
        assertThat(tokenService.validateToken(null)).isFalse();
        assertThat(tokenService.validateToken("")).isFalse();
        assertThat(tokenService.extractClaims("invalid.jwt.token")).isEmpty();
    }

    @Test
    @DisplayName("Should reject expired token")
    void testExpiredToken() throws InterruptedException {
        BearerTokenServiceImpl shortLivedService = new BearerTokenServiceImpl(SECRET_KEY, 10L); // 10ms
        User user = User.registerWithLocalCredentials(
                EmailAddress.of("user@workshop.pe"),
                Password.of(BCRYPT_HASH),
                PersonName.of("Ana", "Rios"),
                PhoneNumber.of("+51999111222")
        );

        String token = shortLivedService.generateToken(user, TenantId.generate(), List.of());
        Thread.sleep(50L);

        assertThat(shortLivedService.validateToken(token)).isFalse();
        assertThat(shortLivedService.extractClaims(token)).isEmpty();
    }
}
