package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.TenantCommandServiceImpl;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateTenantProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link TenantCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tenant Command Service Unit Tests")
class TenantCommandServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private BCryptHashingService hashingService;

    private TenantCommandService tenantCommandService;

    private final TaxId validRuc = TaxId.ruc("20456789014");
    private final EmailAddress adminEmail = EmailAddress.of("admin@autotaller.pe");
    private final Password adminPassword = Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012");
    private final PersonName adminName = PersonName.of("Carlos", "García");
    private final PhoneNumber adminPhone = PhoneNumber.of("+51987654321");

    @BeforeEach
    void setUp() {
        tenantCommandService = new TenantCommandServiceImpl(
                tenantRepository,
                userRepository,
                roleRepository,
                permissionRepository,
                membershipRepository,
                hashingService
        );
    }

    @Test
    @DisplayName("Should create Tenant, default branch, provision roles, and bind admin successfully")
    void shouldCreateTenantSuccessfully() {
        CreateTenantCommand command = new CreateTenantCommand(
                "AutoTaller Pro",
                "AutoTaller Pro S.A.C.",
                validRuc,
                adminEmail,
                adminPassword,
                adminName,
                adminPhone
        );

        when(tenantRepository.existsByTaxId(validRuc)).thenReturn(false);
        when(userRepository.existsByEmail(adminEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Permission perm = Permission.create("mro:work-orders:read", "Read work orders", "OPERATIONS");
        when(permissionRepository.findAll()).thenReturn(List.of(perm));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Tenant tenant = result.getOrThrow();
        assertThat(tenant.name()).isEqualTo("AutoTaller Pro");
        assertThat(tenant.legalName()).isEqualTo("AutoTaller Pro S.A.C.");
        assertThat(tenant.taxId()).isEqualTo(validRuc);
        assertThat(tenant.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.branches()).hasSize(1);
        assertThat(tenant.branches().get(0).name()).isEqualTo("Sede Principal");

        verify(userRepository).save(any(User.class));
        verify(tenantRepository).save(any(Tenant.class));
        verify(roleRepository, times(RoleTemplate.values().length)).save(any(Role.class));
        verify(membershipRepository).save(any());
    }

    @Test
    @DisplayName("Should reject Tenant creation when TaxId already exists")
    void shouldRejectWhenTaxIdAlreadyExists() {
        CreateTenantCommand command = new CreateTenantCommand(
                "AutoTaller Pro",
                "AutoTaller Pro S.A.C.",
                validRuc,
                adminEmail,
                adminPassword,
                adminName,
                adminPhone
        );

        when(tenantRepository.existsByTaxId(validRuc)).thenReturn(true);

        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        assertThat(result.getError().message()).contains(validRuc.value());
        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject Tenant creation when admin email already exists")
    void shouldRejectWhenAdminEmailAlreadyExists() {
        CreateTenantCommand command = new CreateTenantCommand(
                "AutoTaller Pro",
                "AutoTaller Pro S.A.C.",
                validRuc,
                adminEmail,
                adminPassword,
                adminName,
                adminPhone
        );

        when(tenantRepository.existsByTaxId(validRuc)).thenReturn(false);
        when(userRepository.existsByEmail(adminEmail)).thenReturn(true);

        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        assertThat(result.getError().message()).contains(adminEmail.value());
        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update Tenant profile successfully")
    void shouldUpdateTenantProfileSuccessfully() {
        Tenant tenant = Tenant.create("Old Name", "Old Legal S.A.", validRuc);
        TenantId tenantId = tenant.id();
        UpdateTenantProfileCommand command = new UpdateTenantProfileCommand(tenantId, "New Commercial", "New Legal S.A.C.");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Tenant updated = result.getOrThrow();
        assertThat(updated.name()).isEqualTo("New Commercial");
        assertThat(updated.legalName()).isEqualTo("New Legal S.A.C.");
        verify(tenantRepository).save(tenant);
    }

    @Test
    @DisplayName("Should fail updating profile when Tenant is not found")
    void shouldFailUpdatingProfileWhenTenantNotFound() {
        TenantId tenantId = TenantId.generate();
        UpdateTenantProfileCommand command = new UpdateTenantProfileCommand(tenantId, "New Commercial", "New Legal S.A.C.");

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        Result<Tenant, ApplicationError> result = tenantCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
        verify(tenantRepository, never()).save(any());
    }
}
