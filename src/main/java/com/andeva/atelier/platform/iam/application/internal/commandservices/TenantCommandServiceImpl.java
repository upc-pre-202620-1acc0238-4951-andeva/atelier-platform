package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateTenantProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.repositories.PermissionRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.DistanceMeters;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Transactional orchestrator implementing workshop Tenant onboarding and profile updates.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class TenantCommandServiceImpl implements TenantCommandService {

    private static final double DEFAULT_LIMA_LATITUDE = -12.046374;
    private static final double DEFAULT_LIMA_LONGITUDE = -77.042793;
    private static final int DEFAULT_GEOFENCE_RADIUS_METERS = 500;
    private static final String DEFAULT_SUNAT_ESTABLISHMENT_CODE = "0000";
    private static final String DEFAULT_MAIN_BRANCH_NAME = "Sede Principal";

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final TenantMembershipRepository membershipRepository;
    private final BCryptHashingService hashingService;

    public TenantCommandServiceImpl(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            TenantMembershipRepository membershipRepository,
            BCryptHashingService hashingService
    ) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
        this.permissionRepository = Objects.requireNonNull(permissionRepository, "PermissionRepository cannot be null");
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
        this.hashingService = Objects.requireNonNull(hashingService, "BCryptHashingService cannot be null");
    }

    @Override
    public Result<Tenant, ApplicationError> handle(CreateTenantCommand command) {
        Objects.requireNonNull(command, "CreateTenantCommand cannot be null");

        // 1. Verify TaxId (RUC) uniqueness
        if (tenantRepository.existsByTaxId(command.taxId())) {
            return Result.failure(ApplicationError.conflict(
                    "A workshop Tenant with the specified Tax ID (RUC) already exists: " + command.taxId().value()));
        }

        // 2. Verify admin email uniqueness
        if (userRepository.existsByEmail(command.adminEmail())) {
            return Result.failure(ApplicationError.conflict(
                    "A user account with the specified email address already exists: " + command.adminEmail().value()));
        }

        // 3. Create administrator account in ACTIVE state
        User adminUser = User.registerWithLocalCredentials(
                command.adminEmail(),
                command.adminPassword(),
                command.adminName(),
                command.adminPhone()
        );
        adminUser.activate();
        User savedAdmin = userRepository.save(adminUser);

        // 4. Create Tenant aggregate and provision default main branch
        Tenant tenant = Tenant.create(command.name(), command.legalName(), command.taxId());
        GeoPoint defaultLocation = GeoPoint.of(DEFAULT_LIMA_LATITUDE, DEFAULT_LIMA_LONGITUDE);
        tenant.addBranch(DEFAULT_MAIN_BRANCH_NAME, DEFAULT_SUNAT_ESTABLISHMENT_CODE, defaultLocation, DEFAULT_GEOFENCE_RADIUS_METERS);
        Tenant savedTenant = tenantRepository.save(tenant);

        // 5. Provision standard canonical roles for the new tenant
        List<Permission> allPermissions = permissionRepository.findAll();
        Set<Permission> fullPermissionsSet = new HashSet<>(allPermissions);

        Role workshopAdminRole = null;
        for (RoleTemplate template : RoleTemplate.values()) {
            Role role = Role.fromTemplate(savedTenant.id(), template, fullPermissionsSet);
            Role savedRole = roleRepository.save(role);
            if (template == RoleTemplate.ROLE_WORKSHOP_ADMIN) {
                workshopAdminRole = savedRole;
            }
        }

        // If template matching didn't catch, fallback to the first saved role
        if (workshopAdminRole == null) {
            workshopAdminRole = roleRepository.findByTenantId(savedTenant.id()).stream().findFirst().orElseThrow();
        }

        // 6. Create initial membership binding the administrator to the tenant with the admin role
        TenantMembership adminMembership = TenantMembership.create(
                savedTenant.id(),
                savedAdmin.id(),
                SalaryType.FIXED,
                Money.soles(0.00),
                Set.of(workshopAdminRole)
        );
        membershipRepository.save(adminMembership);

        return Result.success(savedTenant);
    }

    @Override
    public Result<Tenant, ApplicationError> handle(UpdateTenantProfileCommand command) {
        Objects.requireNonNull(command, "UpdateTenantProfileCommand cannot be null");

        Optional<Tenant> optionalTenant = tenantRepository.findById(command.tenantId());
        if (optionalTenant.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Tenant", command.tenantId().value().toString()));
        }

        Tenant tenant = optionalTenant.get();
        tenant.updateProfile(command.name(), command.legalName());
        Tenant updatedTenant = tenantRepository.save(tenant);

        return Result.success(updatedTenant);
    }
}
