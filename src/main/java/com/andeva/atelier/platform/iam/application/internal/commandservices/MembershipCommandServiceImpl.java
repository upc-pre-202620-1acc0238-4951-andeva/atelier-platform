package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.MembershipCommandService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.commands.AssignRolesToMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeactivateMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateMembershipCompensationCommand;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Transactional orchestrator implementing staff TenantMembership role assignment and compensation updates.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class MembershipCommandServiceImpl implements MembershipCommandService {

    private final TenantMembershipRepository membershipRepository;
    private final RoleRepository roleRepository;

    public MembershipCommandServiceImpl(
            TenantMembershipRepository membershipRepository,
            RoleRepository roleRepository
    ) {
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
    }

    @Override
    public Result<TenantMembership, ApplicationError> handle(AssignRolesToMembershipCommand command) {
        Objects.requireNonNull(command, "AssignRolesToMembershipCommand cannot be null");

        Optional<TenantMembership> optionalMembership = membershipRepository.findById(command.membershipId());
        if (optionalMembership.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TenantMembership", command.membershipId().value().toString()));
        }

        TenantMembership membership = optionalMembership.get();

        // 1. Resolve all target roles and verify tenant ownership
        Set<Role> targetRoles = new HashSet<>();
        for (RoleId roleId : command.roleIds()) {
            Optional<Role> optionalRole = roleRepository.findById(roleId);
            if (optionalRole.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Role", roleId.value().toString()));
            }
            Role role = optionalRole.get();
            if (!role.tenantId().equals(membership.tenantId())) {
                return Result.failure(ApplicationError.conflict(
                        "Security role '" + role.name() + "' belongs to a different workshop tenant"));
            }
            targetRoles.add(role);
        }

        // 2. Add new roles first (to protect the invariant of at least one active role)
        for (Role role : targetRoles) {
            boolean alreadyAssigned = membership.assignedRoles().stream()
                    .anyMatch(r -> r.id().equals(role.id()));
            if (!alreadyAssigned) {
                membership.assignRole(role);
            }
        }

        // 3. Revoke roles that are no longer in targetRoles
        List<RoleId> rolesToRevoke = new ArrayList<>();
        for (Role currentRole : membership.assignedRoles()) {
            boolean inTarget = targetRoles.stream().anyMatch(r -> r.id().equals(currentRole.id()));
            if (!inTarget) {
                rolesToRevoke.add(currentRole.id());
            }
        }

        for (RoleId roleIdToRevoke : rolesToRevoke) {
            membership.revokeRole(roleIdToRevoke);
        }

        TenantMembership savedMembership = membershipRepository.save(membership);
        return Result.success(savedMembership);
    }

    @Override
    public Result<TenantMembership, ApplicationError> handle(UpdateMembershipCompensationCommand command) {
        Objects.requireNonNull(command, "UpdateMembershipCompensationCommand cannot be null");

        Optional<TenantMembership> optionalMembership = membershipRepository.findById(command.membershipId());
        if (optionalMembership.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TenantMembership", command.membershipId().value().toString()));
        }

        TenantMembership membership = optionalMembership.get();
        membership.updateCompensation(command.salaryType(), command.baseSalary());
        TenantMembership savedMembership = membershipRepository.save(membership);

        return Result.success(savedMembership);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeactivateMembershipCommand command) {
        Objects.requireNonNull(command, "DeactivateMembershipCommand cannot be null");

        Optional<TenantMembership> optionalMembership = membershipRepository.findById(command.membershipId());
        if (optionalMembership.isEmpty()) {
            return Result.failure(ApplicationError.notFound("TenantMembership", command.membershipId().value().toString()));
        }

        TenantMembership membership = optionalMembership.get();
        membership.deactivate();
        membershipRepository.save(membership);

        return Result.success(null);
    }
}
