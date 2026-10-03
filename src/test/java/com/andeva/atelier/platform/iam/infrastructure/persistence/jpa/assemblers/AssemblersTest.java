package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.AuthProvider;
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.ProfilePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.VerificationTokenPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JPA persistence assemblers ensuring lossless bidirectional conversion.
 *
 * @author Joel Huamani Estefanero
 */
class AssemblersTest {

    private static final String BCRYPT_HASH = "$2a$12$e80yq9gZ9uGfF8yq47y.I.vM4.a4V7vX5gD.0N0p4m4J6P2u1r2uS";

    @Test
    @DisplayName("PermissionPersistenceAssembler bidirectional conversion")
    void testPermissionPersistenceAssembler() {
        Permission domain = Permission.of(PermissionId.generate(), "iam:roles:create", "Create roles", "IAM");
        PermissionPersistenceEntity entity = PermissionPersistenceAssembler.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.id().value());
        assertThat(entity.getName()).isEqualTo(domain.name());
        assertThat(entity.getDescription()).isEqualTo(domain.description());
        assertThat(entity.getCategory()).isEqualTo(domain.category());

        Permission mappedBack = PermissionPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(domain.id());
        assertThat(mappedBack.name()).isEqualTo(domain.name());
        assertThat(mappedBack.description()).isEqualTo(domain.description());
        assertThat(mappedBack.category()).isEqualTo(domain.category());

        assertThat(PermissionPersistenceAssembler.toDomain(null)).isNull();
        assertThat(PermissionPersistenceAssembler.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("BranchPersistenceAssembler bidirectional conversion")
    void testBranchPersistenceAssembler() {
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(UUID.randomUUID());
        Branch domain = new Branch(
                BranchId.generate(),
                TenantId.of(tenantEntity.getId()),
                "Sede Principal",
                "0000",
                GeoPoint.of(-12.046374, -77.042793),
                50,
                true
        );

        BranchPersistenceEntity entity = BranchPersistenceAssembler.toEntity(domain, tenantEntity);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.id().value());
        assertThat(entity.getName()).isEqualTo("Sede Principal");
        assertThat(entity.getSunatCode()).isEqualTo("0000");
        assertThat(entity.getLatitude()).isEqualTo(-12.046374);
        assertThat(entity.getLongitude()).isEqualTo(-77.042793);
        assertThat(entity.getGeofenceRadiusMeters()).isEqualTo(50);
        assertThat(entity.isActive()).isTrue();

        Branch mappedBack = BranchPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(domain.id());
        assertThat(mappedBack.tenantId()).isEqualTo(domain.tenantId());
        assertThat(mappedBack.location().latitude()).isEqualTo(domain.location().latitude());
        assertThat(mappedBack.isActive()).isTrue();

        assertThat(BranchPersistenceAssembler.toDomain(null)).isNull();
        assertThat(BranchPersistenceAssembler.toEntity(null, tenantEntity)).isNull();
    }

    @Test
    @DisplayName("TenantPersistenceAssembler bidirectional conversion")
    void testTenantPersistenceAssembler() {
        TenantId tenantId = TenantId.generate();
        Branch branch = new Branch(
                BranchId.generate(),
                tenantId,
                "Sede Surquillo",
                "0000",
                GeoPoint.of(-12.11, -77.02),
                60,
                true
        );

        Tenant tenant = new Tenant(
                tenantId,
                "AutoFix Workshop",
                "AutoFix S.A.C.",
                TaxId.of("20100070970"),
                TenantStatus.ACTIVE,
                "cus_12345",
                List.of(branch)
        );

        TenantPersistenceEntity entity = TenantPersistenceAssembler.toEntity(tenant);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(tenantId.value());
        assertThat(entity.getName()).isEqualTo("AutoFix Workshop");
        assertThat(entity.getLegalName()).isEqualTo("AutoFix S.A.C.");
        assertThat(entity.getTaxId()).isEqualTo("20100070970");
        assertThat(entity.getStatus()).isEqualTo("active");
        assertThat(entity.getBranches()).hasSize(1);

        Tenant mappedBack = TenantPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(tenantId);
        assertThat(mappedBack.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(mappedBack.branches()).hasSize(1);

        assertThat(TenantPersistenceAssembler.toDomain(null)).isNull();
        assertThat(TenantPersistenceAssembler.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("UserPersistenceAssembler bidirectional conversion")
    void testUserPersistenceAssembler() {
        UserId userId = UserId.generate();
        User domain = User.registerWithLocalCredentials(
                EmailAddress.of("user@workshop.pe"),
                Password.of(BCRYPT_HASH),
                PersonName.of("Carlos", "Mendoza"),
                PhoneNumber.of("+51987654321")
        );
        domain.issueVerificationToken(TokenType.EMAIL_VERIFICATION, java.time.Duration.ofMinutes(15));

        UserPersistenceEntity entity = UserPersistenceAssembler.toEntity(domain);
        assertThat(entity).isNotNull();
        assertThat(entity.getEmail()).isEqualTo("user@workshop.pe");
        assertThat(entity.getAuthProvider()).isEqualTo("local");
        assertThat(entity.getProfile()).isNotNull();
        assertThat(entity.getProfile().getFirstName()).isEqualTo("Carlos");
        assertThat(entity.getProfile().getLastName()).isEqualTo("Mendoza");
        assertThat(entity.getProfile().getPhoneNumber()).isEqualTo("+51987654321");
        assertThat(entity.getVerificationTokens()).hasSize(1);

        User mappedBack = UserPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(domain.id());
        assertThat(mappedBack.email().value()).isEqualTo("user@workshop.pe");
        assertThat(mappedBack.profile().fullName()).isEqualTo("Carlos Mendoza");
        assertThat(mappedBack.verificationTokens()).hasSize(1);

        assertThat(UserPersistenceAssembler.toDomain(null)).isNull();
        assertThat(UserPersistenceAssembler.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("RolePersistenceAssembler bidirectional conversion")
    void testRolePersistenceAssembler() {
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(UUID.randomUUID());
        Permission perm = Permission.of("iam:users:read", "Read users", "IAM");
        PermissionPersistenceEntity permEntity = PermissionPersistenceAssembler.toEntity(perm);

        Role role = Role.createCustom(
                TenantId.of(tenantEntity.getId()),
                "Mechanic Lead",
                "Lead patio technician",
                Set.of(perm)
        );

        RolePersistenceEntity entity = RolePersistenceAssembler.toEntity(
                role,
                tenantEntity,
                Set.of(permEntity)
        );
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Mechanic Lead");
        assertThat(entity.getPermissions()).hasSize(1);

        Role mappedBack = RolePersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(role.id());
        assertThat(mappedBack.name()).isEqualTo("Mechanic Lead");
        assertThat(mappedBack.permissions()).hasSize(1);

        assertThat(RolePersistenceAssembler.toDomain(null)).isNull();
        assertThat(RolePersistenceAssembler.toEntity(null, tenantEntity, Set.of())).isNull();
    }

    @Test
    @DisplayName("TenantMembershipPersistenceAssembler bidirectional conversion")
    void testTenantMembershipPersistenceAssembler() {
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(UUID.randomUUID());
        UserPersistenceEntity userEntity = new UserPersistenceEntity(UUID.randomUUID());
        RolePersistenceEntity roleEntity = new RolePersistenceEntity(
                UUID.randomUUID(),
                tenantEntity,
                "ROLE_MECHANIC",
                "Mechanic",
                "Mechanic role",
                true,
                Set.of()
        );
        Role role = RolePersistenceAssembler.toDomain(roleEntity);

        TenantMembership membership = new TenantMembership(
                TenantMembershipId.generate(),
                TenantId.of(tenantEntity.getId()),
                UserId.of(userEntity.getId()),
                MembershipStatus.ACTIVE,
                SalaryType.FIXED,
                Money.soles(2500.00),
                Set.of(role)
        );

        TenantMembershipPersistenceEntity entity = TenantMembershipPersistenceAssembler.toEntity(
                membership,
                tenantEntity,
                userEntity,
                Set.of(roleEntity)
        );
        assertThat(entity).isNotNull();
        assertThat(entity.getBaseSalary()).isEqualByComparingTo("2500.00");
        assertThat(entity.getStatus()).isEqualTo("active");
        assertThat(entity.getAssignedRoles()).hasSize(1);

        TenantMembership mappedBack = TenantMembershipPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(membership.id());
        assertThat(mappedBack.status()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(mappedBack.salaryType()).isEqualTo(SalaryType.FIXED);
        assertThat(mappedBack.baseSalary().amount()).isEqualByComparingTo("2500.00");
        assertThat(mappedBack.assignedRoles()).hasSize(1);

        assertThat(TenantMembershipPersistenceAssembler.toDomain(null)).isNull();
        assertThat(TenantMembershipPersistenceAssembler.toEntity(null, tenantEntity, userEntity, Set.of())).isNull();
    }

    @Test
    @DisplayName("InvitationPersistenceAssembler bidirectional conversion")
    void testInvitationPersistenceAssembler() {
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(UUID.randomUUID());
        Invitation invitation = new Invitation(
                InvitationId.generate(),
                TenantId.of(tenantEntity.getId()),
                EmailAddress.of("staff@workshop.pe"),
                "token-random-crypto-123",
                InvitationStatus.PENDING,
                RoleId.generate(),
                Instant.now().plus(7, ChronoUnit.DAYS)
        );

        InvitationPersistenceEntity entity = InvitationPersistenceAssembler.toEntity(invitation, tenantEntity);
        assertThat(entity).isNotNull();
        assertThat(entity.getEmail()).isEqualTo("staff@workshop.pe");
        assertThat(entity.getToken()).isEqualTo("token-random-crypto-123");
        assertThat(entity.getStatus()).isEqualTo("pending");

        Invitation mappedBack = InvitationPersistenceAssembler.toDomain(entity);
        assertThat(mappedBack).isNotNull();
        assertThat(mappedBack.id()).isEqualTo(invitation.id());
        assertThat(mappedBack.email().value()).isEqualTo("staff@workshop.pe");
        assertThat(mappedBack.status()).isEqualTo(InvitationStatus.PENDING);

        assertThat(InvitationPersistenceAssembler.toDomain(null)).isNull();
        assertThat(InvitationPersistenceAssembler.toEntity(null, tenantEntity)).isNull();
    }
}
