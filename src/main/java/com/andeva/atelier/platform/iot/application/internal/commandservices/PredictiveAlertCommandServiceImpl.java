package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.PredictiveAlertCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.CrmFleetAclPort;
import com.andeva.atelier.platform.iot.domain.exceptions.PredictiveAlertNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ConvertAlertToAppointmentCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.DismissPredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.GeneratePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Command Service implementation for managing predictive alert lifecycle,
 * acknowledgments, dismissals, and appointment conversion.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class PredictiveAlertCommandServiceImpl implements PredictiveAlertCommandService {

    private final PredictiveAlertRepository predictiveAlertRepository;
    private final CrmFleetAclPort crmFleetAclPort;
    private final ApplicationEventPublisher eventPublisher;

    public PredictiveAlertCommandServiceImpl(
            PredictiveAlertRepository predictiveAlertRepository,
            CrmFleetAclPort crmFleetAclPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.predictiveAlertRepository = Objects.requireNonNull(predictiveAlertRepository, "PredictiveAlertRepository cannot be null");
        this.crmFleetAclPort = Objects.requireNonNull(crmFleetAclPort, "CrmFleetAclPort cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public AlertId handle(GeneratePredictiveAlertCommand command) {
        Objects.requireNonNull(command, "GeneratePredictiveAlertCommand cannot be null");

        PredictiveAlert alert = PredictiveAlert.create(
                command.vehicleId(),
                command.tenantId(),
                command.serviceId(),
                command.type(),
                command.score(),
                command.message()
        );
        alert.recordDispatch(null);

        predictiveAlertRepository.save(alert);
        alert.domainEvents().forEach(eventPublisher::publishEvent);

        return alert.getId();
    }

    @Override
    public void handle(AcknowledgePredictiveAlertCommand command) {
        Objects.requireNonNull(command, "AcknowledgePredictiveAlertCommand cannot be null");

        PredictiveAlert alert = predictiveAlertRepository.findById(command.alertId())
                .orElseThrow(() -> new PredictiveAlertNotFoundException(command.alertId()));

        alert.acknowledge();
        predictiveAlertRepository.save(alert);
        alert.domainEvents().forEach(eventPublisher::publishEvent);
    }

    @Override
    public void handle(DismissPredictiveAlertCommand command) {
        Objects.requireNonNull(command, "DismissPredictiveAlertCommand cannot be null");

        PredictiveAlert alert = predictiveAlertRepository.findById(command.alertId())
                .orElseThrow(() -> new PredictiveAlertNotFoundException(command.alertId()));

        alert.dismiss();
        predictiveAlertRepository.save(alert);
        alert.domainEvents().forEach(eventPublisher::publishEvent);
    }

    @Override
    public UUID handle(ConvertAlertToAppointmentCommand command) {
        Objects.requireNonNull(command, "ConvertAlertToAppointmentCommand cannot be null");

        PredictiveAlert alert = predictiveAlertRepository.findById(command.alertId())
                .orElseThrow(() -> new PredictiveAlertNotFoundException(command.alertId()));

        UUID appointmentId = crmFleetAclPort.convertAlertToAppointment(
                alert.getId(),
                alert.getTenantId(),
                alert.getVehicleId(),
                alert.getMessage(),
                null
        );

        alert.resolve();
        predictiveAlertRepository.save(alert);
        alert.domainEvents().forEach(eventPublisher::publishEvent);

        return appointmentId;
    }
}
