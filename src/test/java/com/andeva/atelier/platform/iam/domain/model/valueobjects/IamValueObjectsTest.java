package com.andeva.atelier.platform.iam.domain.model.valueobjects;

import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for IAM & Tenancy Value Objects, Strongly Typed Identifiers, and Enums.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("IAM Value Objects & Enums Unit Tests")
class IamValueObjectsTest {

    @Nested
    @DisplayName("Strongly Typed Identifiers Tests")
    class StronglyTypedIdsTests {

        @Test
        @DisplayName("TenantMembershipId should validate null, generate UUID and equality")
        void shouldHandleTenantMembershipId() {
            UUID uuid = UUID.randomUUID();
            TenantMembershipId id1 = TenantMembershipId.of(uuid);
            TenantMembershipId id2 = TenantMembershipId.of(uuid.toString());

            assertThat(id1).isEqualTo(id2);
            assertThat(id1.value()).isEqualTo(uuid);

            TenantMembershipId generated = TenantMembershipId.generate();
            assertThat(generated.value()).isNotNull();

            assertThatThrownBy(() -> TenantMembershipId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> TenantMembershipId.of((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("RoleId should validate null, generate UUID and equality")
        void shouldHandleRoleId() {
            UUID uuid = UUID.randomUUID();
            RoleId r1 = RoleId.of(uuid);
            RoleId r2 = RoleId.of(uuid.toString());

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.value()).isEqualTo(uuid);

            RoleId generated = RoleId.generate();
            assertThat(generated.value()).isNotNull();

            assertThatThrownBy(() -> RoleId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> RoleId.of((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("PermissionId should validate null, generate UUID and equality")
        void shouldHandlePermissionId() {
            UUID uuid = UUID.randomUUID();
            PermissionId p1 = PermissionId.of(uuid);
            PermissionId p2 = PermissionId.of(uuid.toString());

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.value()).isEqualTo(uuid);

            PermissionId generated = PermissionId.generate();
            assertThat(generated.value()).isNotNull();

            assertThatThrownBy(() -> PermissionId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> PermissionId.of((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("InvitationId should validate null, generate UUID and equality")
        void shouldHandleInvitationId() {
            UUID uuid = UUID.randomUUID();
            InvitationId i1 = InvitationId.of(uuid);
            InvitationId i2 = InvitationId.of(uuid.toString());

            assertThat(i1).isEqualTo(i2);
            assertThat(i1.value()).isEqualTo(uuid);

            InvitationId generated = InvitationId.generate();
            assertThat(generated.value()).isNotNull();

            assertThatThrownBy(() -> InvitationId.of((UUID) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> InvitationId.of((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Password Value Object Tests")
    class PasswordTests {

        private final String validBcrypt = "$2a$12$e8uqYVq9YwQf49tGeqr0yOXM33H7jQyH56dK50BupL2a/o9wQkX3S";

        @Test
        @DisplayName("Should instantiate with valid BCrypt hash and mask in toString")
        void shouldInstantiateWithValidBcrypt() {
            Password password = Password.of(validBcrypt);
            assertThat(password.hashedValue()).isEqualTo(validBcrypt);
            assertThat(password.toString()).isEqualTo("[PROTECTED_CREDENTIAL]");
        }

        @Test
        @DisplayName("Should reject null or empty hash")
        void shouldRejectNullOrEmptyHash() {
            assertThatThrownBy(() -> Password.of(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Password.of("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject raw plaintext or malformed BCrypt hash")
        void shouldRejectMalformedBcrypt() {
            assertThatThrownBy(() -> Password.of("PlaintextSecret123!"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid BCrypt password hash format");

            assertThatThrownBy(() -> Password.of("$1$invalid$hash"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("PersonName Value Object Tests")
    class PersonNameTests {

        @Test
        @DisplayName("Should instantiate valid name, trim whitespace, and provide full name")
        void shouldInstantiateValidPersonName() {
            PersonName name = PersonName.of("  Carlos  ", "  García López  ");
            assertThat(name.firstName()).isEqualTo("Carlos");
            assertThat(name.lastName()).isEqualTo("García López");
            assertThat(name.getFullName()).isEqualTo("Carlos García López");
        }

        @Test
        @DisplayName("Should reject null or names shorter than 2 characters")
        void shouldRejectInvalidPersonName() {
            assertThatThrownBy(() -> PersonName.of(null, "Perez"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> PersonName.of("Juan", null))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> PersonName.of("J", "Perez"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("First name must contain at least 2 characters");

            assertThatThrownBy(() -> PersonName.of("Juan", "P"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Last name must contain at least 2 characters");
        }
    }

    @Nested
    @DisplayName("Domain Enums Tests")
    class DomainEnumsTests {

        @Test
        @DisplayName("TenantStatus should contain expected values")
        void shouldContainTenantStatuses() {
            assertThat(TenantStatus.values()).containsExactly(
                    TenantStatus.PENDING,
                    TenantStatus.ACTIVE,
                    TenantStatus.SUSPENDED
            );
        }

        @Test
        @DisplayName("UserStatus should contain expected values")
        void shouldContainUserStatuses() {
            assertThat(UserStatus.values()).containsExactly(
                    UserStatus.PENDING_VERIFICATION,
                    UserStatus.ACTIVE,
                    UserStatus.SUSPENDED
            );
        }

        @Test
        @DisplayName("MembershipStatus should contain expected values")
        void shouldContainMembershipStatuses() {
            assertThat(MembershipStatus.values()).containsExactly(
                    MembershipStatus.ACTIVE,
                    MembershipStatus.INACTIVE
            );
        }

        @Test
        @DisplayName("SalaryType should contain expected values")
        void shouldContainSalaryTypes() {
            assertThat(SalaryType.values()).containsExactly(
                    SalaryType.FIXED,
                    SalaryType.HOURLY
            );
        }

        @Test
        @DisplayName("TokenType should contain expected values")
        void shouldContainTokenTypes() {
            assertThat(TokenType.values()).containsExactly(
                    TokenType.EMAIL_VERIFICATION,
                    TokenType.PASSWORD_RESET,
                    TokenType.LOGIN_OTP
            );
        }

        @Test
        @DisplayName("InvitationStatus should contain expected values")
        void shouldContainInvitationStatuses() {
            assertThat(InvitationStatus.values()).containsExactly(
                    InvitationStatus.PENDING,
                    InvitationStatus.ACCEPTED,
                    InvitationStatus.EXPIRED,
                    InvitationStatus.REVOKED
            );
        }

        @Test
        @DisplayName("AuthProvider should contain expected values")
        void shouldContainAuthProviders() {
            assertThat(AuthProvider.values()).containsExactly(
                    AuthProvider.LOCAL,
                    AuthProvider.GOOGLE
            );
        }

        @Test
        @DisplayName("RoleTemplate should contain standard factory templates with metadata")
        void shouldContainRoleTemplates() {
            assertThat(RoleTemplate.values()).hasSize(5);

            RoleTemplate owner = RoleTemplate.ROLE_WORKSHOP_OWNER;
            assertThat(owner.code()).isEqualTo("ROLE_WORKSHOP_OWNER");
            assertThat(owner.defaultName()).isEqualTo("Workshop Owner");
            assertThat(owner.description()).isNotEmpty();

            RoleTemplate mechanic = RoleTemplate.ROLE_MECHANIC;
            assertThat(mechanic.code()).isEqualTo("ROLE_MECHANIC");
            assertThat(mechanic.defaultName()).isEqualTo("Mechanic");
        }
    }
}
