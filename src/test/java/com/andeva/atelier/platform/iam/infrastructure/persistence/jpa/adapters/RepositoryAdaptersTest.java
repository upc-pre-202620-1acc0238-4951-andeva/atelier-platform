package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.BranchPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.InvitationPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.PermissionPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.RolePersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.TenantMembershipPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.TenantPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantMembershipPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.BranchPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.InvitationPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.PermissionPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantMembershipPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
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
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for domain repository adapters delegating to Spring Data JPA repositories.
 *
 * @author Joel Huamani Estefanero
 */
class RepositoryAdaptersTest {

    private static final String BCRYPT_HASH = "$2a$12$e80yq9gZ9uGfF8yq47y.I.vM4.a4V7vX5gD.0N0p4m4J6P2u1r2uS";

    @Test
    @DisplayName("TenantRepositoryImpl executes save, findById, findByTaxId and existsByTaxId")
    void testTenantRepositoryImpl() {
        TenantPersistenceRepository repo = mock(TenantPersistenceRepository.class);
        TenantRepositoryImpl adapter = new TenantRepositoryImpl(repo);

        Tenant tenant = Tenant.create("AutoFix", "AutoFix SAC", TaxId.of("20100070970"));
        TenantPersistenceEntity entity = TenantPersistenceAssembler.toEntity(tenant);

        when(repo.save(any(TenantPersistenceEntity.class))).thenReturn(entity);
        when(repo.findById(tenant.id().value())).thenReturn(Optional.of(entity));
        when(repo.findByTaxId("20100070970")).thenReturn(Optional.of(entity));
        when(repo.existsByTaxId("20100070970")).thenReturn(true);

        Tenant saved = adapter.save(tenant);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(tenant.id());

        assertThat(adapter.findById(tenant.id())).isPresent();
        assertThat(adapter.findByTaxId(TaxId.of("20100070970"))).isPresent();
        assertThat(adapter.existsByTaxId(TaxId.of("20100070970"))).isTrue();
    }

    @Test
    @DisplayName("BranchRepositoryImpl executes save, findById and findByTenantId")
    void testBranchRepositoryImpl() {
        BranchPersistenceRepository branchRepo = mock(BranchPersistenceRepository.class);
        TenantPersistenceRepository tenantRepo = mock(TenantPersistenceRepository.class);
        BranchRepositoryImpl adapter = new BranchRepositoryImpl(branchRepo, tenantRepo);

        TenantId tenantId = TenantId.generate();
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(tenantId.value());
        Branch branch = Branch.create(tenantId, "Sede Central", "0000", GeoPoint.of(-12.0, -77.0), 50);
        BranchPersistenceEntity branchEntity = BranchPersistenceAssembler.toEntity(branch, tenantEntity);

        when(tenantRepo.getReferenceById(tenantId.value())).thenReturn(tenantEntity);
        when(branchRepo.save(any(BranchPersistenceEntity.class))).thenReturn(branchEntity);
        when(branchRepo.findById(branch.id().value())).thenReturn(Optional.of(branchEntity));
        when(branchRepo.findByTenant_Id(tenantId.value())).thenReturn(List.of(branchEntity));

        Branch saved = adapter.save(branch);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(branch.id());

        assertThat(adapter.findById(branch.id())).isPresent();
        assertThat(adapter.findByTenantId(tenantId)).hasSize(1);
    }

    @Test
    @DisplayName("UserRepositoryImpl executes save, findById, findByEmail and existsByEmail")
    void testUserRepositoryImpl() {
        UserPersistenceRepository userRepo = mock(UserPersistenceRepository.class);
        UserRepositoryImpl adapter = new UserRepositoryImpl(userRepo);

        User user = User.registerWithLocalCredentials(
                EmailAddress.of("user@workshop.pe"),
                Password.of(BCRYPT_HASH),
                PersonName.of("David", "Perez"),
                PhoneNumber.of("+51987111222")
        );
        UserPersistenceEntity entity = UserPersistenceAssembler.toEntity(user);

        when(userRepo.save(any(UserPersistenceEntity.class))).thenReturn(entity);
        when(userRepo.findById(user.id().value())).thenReturn(Optional.of(entity));
        when(userRepo.findByEmail("user@workshop.pe")).thenReturn(Optional.of(entity));
        when(userRepo.existsByEmail("user@workshop.pe")).thenReturn(true);
        when(userRepo.findByGoogleId("google-123")).thenReturn(Optional.of(entity));
        when(userRepo.findByVerificationToken("otp-123")).thenReturn(Optional.of(entity));

        User saved = adapter.save(user);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(user.id());

        assertThat(adapter.findById(user.id())).isPresent();
        assertThat(adapter.findByEmail(EmailAddress.of("user@workshop.pe"))).isPresent();
        assertThat(adapter.existsByEmail(EmailAddress.of("user@workshop.pe"))).isTrue();
        assertThat(adapter.findByGoogleId("google-123")).isPresent();
        assertThat(adapter.findByVerificationToken("otp-123")).isPresent();
    }

    @Test
    @DisplayName("RoleRepositoryImpl executes save, findById, findByTenantIdAndName and delete")
    void testRoleRepositoryImpl() {
        RolePersistenceRepository roleRepo = mock(RolePersistenceRepository.class);
        TenantPersistenceRepository tenantRepo = mock(TenantPersistenceRepository.class);
        PermissionPersistenceRepository permRepo = mock(PermissionPersistenceRepository.class);
        RoleRepositoryImpl adapter = new RoleRepositoryImpl(roleRepo, tenantRepo, permRepo);

        TenantId tenantId = TenantId.generate();
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(tenantId.value());
        Role role = Role.createCustom(tenantId, "Advisor", "Service advisor", Set.of());
        RolePersistenceEntity roleEntity = RolePersistenceAssembler.toEntity(role, tenantEntity, Set.of());

        when(tenantRepo.getReferenceById(tenantId.value())).thenReturn(tenantEntity);
        when(roleRepo.save(any(RolePersistenceEntity.class))).thenReturn(roleEntity);
        when(roleRepo.findById(role.id().value())).thenReturn(Optional.of(roleEntity));
        when(roleRepo.findByTenant_IdAndName(tenantId.value(), "Advisor")).thenReturn(Optional.of(roleEntity));
        when(roleRepo.findByTenant_IdAndCode(tenantId.value(), "ROLE_ADVISOR")).thenReturn(Optional.of(roleEntity));
        when(roleRepo.findByTenant_Id(tenantId.value())).thenReturn(List.of(roleEntity));
        when(roleRepo.existsByTenant_IdAndName(tenantId.value(), "Advisor")).thenReturn(true);

        Role saved = adapter.save(role);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(role.id());

        assertThat(adapter.findById(role.id())).isPresent();
        assertThat(adapter.findByTenantIdAndName(tenantId, "Advisor")).isPresent();
        assertThat(adapter.findByTenantIdAndCode(tenantId, "ROLE_ADVISOR")).isPresent();
        assertThat(adapter.findByTenantId(tenantId)).hasSize(1);
        assertThat(adapter.existsByTenantIdAndName(tenantId, "Advisor")).isTrue();

        adapter.delete(role);
        verify(roleRepo).deleteById(role.id().value());
    }

    @Test
    @DisplayName("TenantMembershipRepositoryImpl executes save, findById, findByTenantIdAndUserId and countByTenantId")
    void testTenantMembershipRepositoryImpl() {
        TenantMembershipPersistenceRepository memberRepo = mock(TenantMembershipPersistenceRepository.class);
        TenantPersistenceRepository tenantRepo = mock(TenantPersistenceRepository.class);
        UserPersistenceRepository userRepo = mock(UserPersistenceRepository.class);
        RolePersistenceRepository roleRepo = mock(RolePersistenceRepository.class);
        TenantMembershipRepositoryImpl adapter = new TenantMembershipRepositoryImpl(
                memberRepo, tenantRepo, userRepo, roleRepo
        );

        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(tenantId.value());
        UserPersistenceEntity userEntity = new UserPersistenceEntity(userId.value());
        Role role = Role.createCustom(tenantId, "Tech", "Technician", Set.of());

        TenantMembership membership = new TenantMembership(
                TenantMembershipId.generate(),
                tenantId,
                userId,
                MembershipStatus.ACTIVE,
                SalaryType.FIXED,
                Money.soles(3000.00),
                Set.of(role)
        );
        RolePersistenceEntity roleEntity = RolePersistenceAssembler.toEntity(role, tenantEntity, Set.of());
        TenantMembershipPersistenceEntity entity = TenantMembershipPersistenceAssembler.toEntity(
                membership, tenantEntity, userEntity, Set.of(roleEntity)
        );

        when(tenantRepo.getReferenceById(tenantId.value())).thenReturn(tenantEntity);
        when(userRepo.getReferenceById(userId.value())).thenReturn(userEntity);
        when(memberRepo.save(any(TenantMembershipPersistenceEntity.class))).thenReturn(entity);
        when(memberRepo.findById(membership.id().value())).thenReturn(Optional.of(entity));
        when(memberRepo.findByTenant_IdAndUser_Id(tenantId.value(), userId.value())).thenReturn(Optional.of(entity));
        when(memberRepo.findByTenant_Id(tenantId.value())).thenReturn(List.of(entity));
        when(memberRepo.findByUser_Id(userId.value())).thenReturn(List.of(entity));
        when(memberRepo.countByTenant_Id(tenantId.value())).thenReturn(1L);

        TenantMembership saved = adapter.save(membership);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(membership.id());

        assertThat(adapter.findById(membership.id())).isPresent();
        assertThat(adapter.findByTenantIdAndUserId(tenantId, userId)).isPresent();
        assertThat(adapter.findByTenantId(tenantId)).hasSize(1);
        assertThat(adapter.findByUserId(userId)).hasSize(1);
        assertThat(adapter.countByTenantId(tenantId)).isEqualTo(1L);
    }

    @Test
    @DisplayName("PermissionRepositoryImpl executes findAll, findByIdIn, findByName and save")
    void testPermissionRepositoryImpl() {
        PermissionPersistenceRepository repo = mock(PermissionPersistenceRepository.class);
        PermissionRepositoryImpl adapter = new PermissionRepositoryImpl(repo);

        Permission perm = Permission.of("iam:permissions:read", "Read permissions", "IAM");
        PermissionPersistenceEntity entity = PermissionPersistenceAssembler.toEntity(perm);

        when(repo.save(any(PermissionPersistenceEntity.class))).thenReturn(entity);
        when(repo.findAll()).thenReturn(List.of(entity));
        when(repo.findByIdIn(List.of(perm.id().value()))).thenReturn(List.of(entity));
        when(repo.findByName("iam:permissions:read")).thenReturn(Optional.of(entity));

        Permission saved = adapter.save(perm);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(perm.id());

        assertThat(adapter.findAll()).hasSize(1);
        assertThat(adapter.findByIdIn(List.of(perm.id()))).hasSize(1);
        assertThat(adapter.findByName("iam:permissions:read")).isPresent();
    }

    @Test
    @DisplayName("InvitationRepositoryImpl executes save, findById, findByToken and findByTenantId")
    void testInvitationRepositoryImpl() {
        InvitationPersistenceRepository invRepo = mock(InvitationPersistenceRepository.class);
        TenantPersistenceRepository tenantRepo = mock(TenantPersistenceRepository.class);
        InvitationRepositoryImpl adapter = new InvitationRepositoryImpl(invRepo, tenantRepo);

        TenantId tenantId = TenantId.generate();
        TenantPersistenceEntity tenantEntity = new TenantPersistenceEntity(tenantId.value());
        Invitation invitation = new Invitation(
                InvitationId.generate(),
                tenantId,
                EmailAddress.of("invited@atelier.pe"),
                "token-random-12345",
                InvitationStatus.PENDING,
                RoleId.generate(),
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        InvitationPersistenceEntity entity = InvitationPersistenceAssembler.toEntity(invitation, tenantEntity);

        when(tenantRepo.getReferenceById(tenantId.value())).thenReturn(tenantEntity);
        when(invRepo.save(any(InvitationPersistenceEntity.class))).thenReturn(entity);
        when(invRepo.findById(invitation.id().value())).thenReturn(Optional.of(entity));
        when(invRepo.findByToken("token-random-12345")).thenReturn(Optional.of(entity));
        when(invRepo.findByTenant_IdAndEmail(tenantId.value(), "invited@atelier.pe")).thenReturn(Optional.of(entity));
        when(invRepo.findByTenant_Id(tenantId.value())).thenReturn(List.of(entity));

        Invitation saved = adapter.save(invitation);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(invitation.id());

        assertThat(adapter.findById(invitation.id())).isPresent();
        assertThat(adapter.findByToken("token-random-12345")).isPresent();
        assertThat(adapter.findByTenantIdAndEmail(tenantId, EmailAddress.of("invited@atelier.pe"))).isPresent();
        assertThat(adapter.findByTenantId(tenantId)).hasSize(1);
    }
}
