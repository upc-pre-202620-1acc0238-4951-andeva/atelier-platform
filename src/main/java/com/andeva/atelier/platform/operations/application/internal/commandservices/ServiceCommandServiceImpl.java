package com.andeva.atelier.platform.operations.application.internal.commandservices;

import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@org.springframework.stereotype.Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class ServiceCommandServiceImpl implements ServiceCommandService {

    private final ServiceRepository serviceRepository;

    public ServiceCommandServiceImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = Objects.requireNonNull(serviceRepository, "ServiceRepository cannot be null");
    }

    @Override
    public Result<Service, ApplicationError> handle(CreateServiceItemCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("CreateServiceItemCommand cannot be null"));
        }

        if (serviceRepository.findByTenantIdAndName(command.tenantId(), command.name()).isPresent()) {
            return Result.failure(ApplicationError.conflict("Service with name '" + command.name() + "' already exists in this workshop"));
        }

        Service service = Service.create(command.tenantId(), command.name(), Money.soles(command.basePrice()), command.estimatedMinutes());
        Service saved = serviceRepository.save(service);
        return Result.success(saved);
    }

    @Override
    public Result<Service, ApplicationError> handle(UpdateServiceItemCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.badRequest("UpdateServiceItemCommand cannot be null"));
        }

        return serviceRepository.findById(command.serviceId())
                .map(service -> {
                    service.updateDetails(command.name(), Money.soles(command.basePrice()), command.estimatedMinutes());
                    Service updated = serviceRepository.save(service);
                    return Result.<Service, ApplicationError>success(updated);
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Service with identifier " + command.serviceId().value() + " was not found")));
    }
}
