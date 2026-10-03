package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.repositories.BranchRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Transactional orchestrator implementing workshop Branch creation, location, and geofence updates.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class BranchCommandServiceImpl implements BranchCommandService {

    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final SubscriptionQuotaPort subscriptionQuotaPort;

    public BranchCommandServiceImpl(
            TenantRepository tenantRepository,
            BranchRepository branchRepository,
            SubscriptionQuotaPort subscriptionQuotaPort
    ) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository cannot be null");
        this.branchRepository = Objects.requireNonNull(branchRepository, "BranchRepository cannot be null");
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "SubscriptionQuotaPort cannot be null");
    }

    @Override
    public Result<Branch, ApplicationError> handle(CreateBranchCommand command) {
        Objects.requireNonNull(command, "CreateBranchCommand cannot be null");

        Optional<Tenant> optionalTenant = tenantRepository.findById(command.tenantId());
        if (optionalTenant.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Tenant", command.tenantId().value().toString()));
        }

        Tenant tenant = optionalTenant.get();

        try {
            subscriptionQuotaPort.validateBranchCreationAllowed(command.tenantId(), tenant.branches().size());
        } catch (Exception ex) {
            return Result.failure(ApplicationError.forbidden(
                    "Branch creation rejected due to subscription quota limits: " + ex.getMessage()));
        }

        Branch branch = tenant.addBranch(
                command.name(),
                command.sunatCode(),
                command.location(),
                command.geofenceRadiusMeters()
        );
        tenantRepository.save(tenant);
        branchRepository.save(branch);

        return Result.success(branch);
    }

    @Override
    public Result<Branch, ApplicationError> handle(UpdateBranchLocationCommand command) {
        Objects.requireNonNull(command, "UpdateBranchLocationCommand cannot be null");

        Optional<Branch> optionalBranch = branchRepository.findById(command.branchId());
        if (optionalBranch.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Branch", command.branchId().value().toString()));
        }

        Branch branch = optionalBranch.get();
        branch.updateDetails(
                command.name(),
                command.sunatCode(),
                command.location(),
                command.geofenceRadiusMeters()
        );
        Branch savedBranch = branchRepository.save(branch);

        return Result.success(savedBranch);
    }
}
