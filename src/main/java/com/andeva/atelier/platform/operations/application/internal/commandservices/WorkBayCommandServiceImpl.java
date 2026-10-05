package com.andeva.atelier.platform.operations.application.internal.commandservices;

import com.andeva.atelier.platform.operations.application.commandservices.WorkBayCommandService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateWorkBayStatusCommand;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class WorkBayCommandServiceImpl implements WorkBayCommandService {

    private final WorkBayRepository workBayRepository;

    public WorkBayCommandServiceImpl(WorkBayRepository workBayRepository) {
        this.workBayRepository = Objects.requireNonNull(workBayRepository, "WorkBayRepository cannot be null");
    }

    @Override
    public Result<WorkBay, ApplicationError> handle(CreateWorkBayCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("CreateWorkBayCommand cannot be null"));
        }

        WorkBay bay = WorkBay.create(command.tenantId(), command.branchId(), command.name(), command.bayType());
        WorkBay saved = workBayRepository.save(bay);
        return Result.success(saved);
    }

    @Override
    public Result<WorkBay, ApplicationError> handle(UpdateWorkBayStatusCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("UpdateWorkBayStatusCommand cannot be null"));
        }

        return workBayRepository.findById(command.bayId())
                .map(bay -> {
                    switch (command.status()) {
                        case AVAILABLE -> bay.restoreAvailable();
                        case MAINTENANCE -> bay.setUnderMaintenance(command.reason());
                        default -> {}
                    }
                    WorkBay updated = workBayRepository.save(bay);
                    return Result.<WorkBay, ApplicationError>success(updated);
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("WorkBay with identifier " + command.bayId().value() + " was not found")));
    }
}
