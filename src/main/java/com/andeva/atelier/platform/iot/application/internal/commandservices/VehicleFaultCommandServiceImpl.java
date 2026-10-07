package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.VehicleFaultCommandService;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleFaultNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ResolveVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.repositories.DtcCatalogRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.domain.services.DtcCodeEvaluationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Command Service implementation for logging vehicle DTC faults and managing their resolution.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class VehicleFaultCommandServiceImpl implements VehicleFaultCommandService {

    private final VehicleFaultRepository vehicleFaultRepository;
    private final DtcCatalogRepository dtcCatalogRepository;
    private final DtcCodeEvaluationService dtcCodeEvaluationService;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleFaultCommandServiceImpl(
            VehicleFaultRepository vehicleFaultRepository,
            DtcCatalogRepository dtcCatalogRepository,
            DtcCodeEvaluationService dtcCodeEvaluationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.vehicleFaultRepository = Objects.requireNonNull(vehicleFaultRepository, "VehicleFaultRepository cannot be null");
        this.dtcCatalogRepository = Objects.requireNonNull(dtcCatalogRepository, "DtcCatalogRepository cannot be null");
        this.dtcCodeEvaluationService = Objects.requireNonNull(dtcCodeEvaluationService, "DtcCodeEvaluationService cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public FaultId handle(RegisterVehicleFaultCommand command) {
        Objects.requireNonNull(command, "RegisterVehicleFaultCommand cannot be null");

        // 1. Resolve standardized metadata from DTC catalog or evaluate with domain service
        Optional<DtcCatalogEntry> catalogEntry = dtcCatalogRepository.findByCode(command.code());
        FaultSeverity severity = catalogEntry.map(DtcCatalogEntry::getDefaultSeverity)
                .orElseGet(() -> dtcCodeEvaluationService.evaluateSeverity(command.code()));

        String description = catalogEntry.map(DtcCatalogEntry::getStandardDescription)
                .filter(desc -> !desc.isBlank())
                .orElse(command.description().isBlank()
                        ? "Diagnostic trouble code " + command.code().value()
                        : command.description());

        // 2. Instantiate and persist aggregate root
        VehicleFault fault = VehicleFault.detect(
                command.vehicleId(),
                command.tenantId(),
                command.code(),
                severity,
                description
        );

        vehicleFaultRepository.save(fault);
        fault.domainEvents().forEach(eventPublisher::publishEvent);

        return fault.getId();
    }

    @Override
    public void handle(ResolveVehicleFaultCommand command) {
        Objects.requireNonNull(command, "ResolveVehicleFaultCommand cannot be null");

        VehicleFault fault = vehicleFaultRepository.findById(command.faultId())
                .orElseThrow(() -> new VehicleFaultNotFoundException(command.faultId()));

        fault.markResolved();
        vehicleFaultRepository.save(fault);
        fault.domainEvents().forEach(eventPublisher::publishEvent);
    }
}
