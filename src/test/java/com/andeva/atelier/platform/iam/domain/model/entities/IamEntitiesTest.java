package com.andeva.atelier.platform.iam.domain.model.entities;

import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for IAM & Tenancy internal Entities (Branch, Profile, VerificationToken, Permission).
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("IAM Internal Entities Unit Tests")
class IamEntitiesTest {

    @Nested
    @DisplayName("Branch Entity Tests")
    class BranchTests {

        private final TenantId tenantId = TenantId.generate();
        private final GeoPoint surquilloLocation = GeoPoint.of(-12.1121, -77.0145);

        @Test
        @DisplayName("Should create branch and accurately evaluate Haversine geofence")
        void shouldCreateBranchAndEvaluateGeofence() {
            Branch branch = Branch.create(tenantId, "Sede Surquillo", "0001", surquilloLocation, 150);

            assertThat(branch.id()).isNotNull();
            assertThat(branch.tenantId()).isEqualTo(tenantId);
            assertThat(branch.name()).isEqualTo("Sede Surquillo");
            assertThat(branch.sunatCode()).isEqualTo("0001");
            assertThat(branch.isActive()).isTrue();

            // Coordinate ~50 meters away
            GeoPoint closeCoords = GeoPoint.of(-12.1123, -77.0143);
            assertThat(branch.isWithinGeofence(closeCoords)).isTrue();

            // Coordinate in Miraflores ~2.5km away
            GeoPoint farCoords = GeoPoint.of(-12.1221, -77.0305);
            assertThat(branch.isWithinGeofence(farCoords)).isFalse();
        }

        @Test
        @DisplayName("Should enforce Branch invariants")
        void shouldEnforceBranchInvariants() {
            assertThatThrownBy(() -> Branch.create(tenantId, " ", "0001", surquilloLocation, 100))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Branch.create(tenantId, "A".repeat(101), "0001", surquilloLocation, 100))
                    .isInstanceOf(IllegalArgumentException.class);

            // SUNAT establishment code must be 4 digits
            assertThatThrownBy(() -> Branch.create(tenantId, "Sede", "123", surquilloLocation, 100))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Branch.create(tenantId, "Sede", "ABCD", surquilloLocation, 100))
                    .isInstanceOf(IllegalArgumentException.class);

            // Geofence radius must be positive
            assertThatThrownBy(() -> Branch.create(tenantId, "Sede", "0001", surquilloLocation, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Branch.create(tenantId, "Sede", "0001", surquilloLocation, -50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should update branch details and toggle activation state")
        void shouldUpdateBranchDetailsAndToggle() {
            Branch branch = Branch.create(tenantId, "Sede Inicial", "0001", surquilloLocation, 100);

            GeoPoint newLocation = GeoPoint.of(-12.1130, -77.0150);
            branch.updateDetails("Sede Renovada", "0002", newLocation, 250);

            assertThat(branch.name()).isEqualTo("Sede Renovada");
            assertThat(branch.sunatCode()).isEqualTo("0002");
            assertThat(branch.location()).isEqualTo(newLocation);
            assertThat(branch.geofenceRadiusMeters()).isEqualTo(250);

            branch.deactivate();
            assertThat(branch.isActive()).isFalse();

            branch.activate();
            assertThat(branch.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Profile Entity Tests")
    class ProfileTests {

        @Test
        @DisplayName("Should create and update user profile")
        void shouldCreateAndUpdateProfile() {
            UserId userId = UserId.generate();
            PersonName name = PersonName.of("Lucía", "Mendoza");
            PhoneNumber phone = PhoneNumber.of("+51987654321");

            Profile profile = Profile.create(userId, name, phone);
            assertThat(profile.userId()).isEqualTo(userId);
            assertThat(profile.getFullName()).isEqualTo("Lucía Mendoza");
            assertThat(profile.phone()).isEqualTo(phone);

            PersonName updatedName = PersonName.of("Lucía María", "Mendoza Quispe");
            PhoneNumber updatedPhone = PhoneNumber.of("+51912345678");
            profile.update(updatedName, updatedPhone);

            assertThat(profile.getFullName()).isEqualTo("Lucía María Mendoza Quispe");
            assertThat(profile.phone()).isEqualTo(updatedPhone);
        }
    }

    @Nested
    @DisplayName("VerificationToken Entity Tests")
    class VerificationTokenTests {

        @Test
        @DisplayName("Should correctly validate token lifecycle and consumption")
        void shouldValidateTokenLifecycle() {
            UserId userId = UserId.generate();
            Instant futureExpiry = Instant.now().plus(15, ChronoUnit.MINUTES);

            VerificationToken token = VerificationToken.issue(userId, "987654", TokenType.EMAIL_VERIFICATION, futureExpiry);

            assertThat(token.id()).isNotNull();
            assertThat(token.userId()).isEqualTo(userId);
            assertThat(token.tokenValue()).isEqualTo("987654");
            assertThat(token.type()).isEqualTo(TokenType.EMAIL_VERIFICATION);
            assertThat(token.isUsed()).isFalse();
            assertThat(token.isValid()).isTrue();

            token.consume();
            assertThat(token.isUsed()).isTrue();
            assertThat(token.isValid()).isFalse();
        }

        @Test
        @DisplayName("Expired token should not be valid")
        void expiredTokenShouldNotBeValid() {
            UserId userId = UserId.generate();
            Instant pastExpiry = Instant.now().minus(5, ChronoUnit.MINUTES);

            VerificationToken expiredToken = VerificationToken.issue(userId, "123456", TokenType.LOGIN_OTP, pastExpiry);
            assertThat(expiredToken.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Permission Catalog Entity Tests")
    class PermissionTests {

        @Test
        @DisplayName("Should create permission, normalize category, and compare equality by name")
        void shouldCreatePermission() {
            Permission p1 = Permission.of("mro:work-orders:create", "Create work orders", "operations");
            Permission p2 = Permission.of(PermissionId.generate(), "mro:work-orders:create", "Another desc", "billing");

            assertThat(p1.category()).isEqualTo("OPERATIONS");
            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());

            assertThatThrownBy(() -> Permission.of(" ", "desc", "cat"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
