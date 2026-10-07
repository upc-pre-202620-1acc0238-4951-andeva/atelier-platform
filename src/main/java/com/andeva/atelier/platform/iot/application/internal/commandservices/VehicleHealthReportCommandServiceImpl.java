package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.AiInferenceDiagnosticPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleHealthReportGeneratedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Command Service implementation orchestrating predictive vehicle health evaluations via Spring AI and Groq LPU.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class VehicleHealthReportCommandServiceImpl implements VehicleHealthReportCommandService {

    private final IoTSubscriptionQuotaPort subscriptionQuotaPort;
    private final AiInferenceDiagnosticPort aiInferenceDiagnosticPort;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleHealthReportCommandServiceImpl(
            IoTSubscriptionQuotaPort subscriptionQuotaPort,
            AiInferenceDiagnosticPort aiInferenceDiagnosticPort,
            ApplicationEventPublisher eventPublisher
    ) {
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "IoTSubscriptionQuotaPort cannot be null");
        this.aiInferenceDiagnosticPort = Objects.requireNonNull(aiInferenceDiagnosticPort, "AiInferenceDiagnosticPort cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public VehicleHealthReportAiDto handle(GenerateVehicleHealthReportCommand command) {
        Objects.requireNonNull(command, "GenerateVehicleHealthReportCommand cannot be null");

        // 1. Enforce SaaS subscription plan quota for monthly predictive AI reports
        subscriptionQuotaPort.validateAiReportGenerationAllowed(command.tenantId(), 0);

        // 2. Perform deep multi-signal Groq LPU diagnostic evaluation
        VehicleHealthReportAiDto report = aiInferenceDiagnosticPort.generateVehicleDiagnostic(
                command.tenantId(),
                command.vehicleId(),
                command.daysToAnalyze(),
                command.includeResolvedDtcHistory()
        );

        // 3. Publish integration event for cross-bounded-context awareness (CRM, MRO, Invoicing)
        eventPublisher.publishEvent(new VehicleHealthReportGeneratedIntegrationEvent(
                command.tenantId().value(),
                command.vehicleId().value(),
                report.healthScore(),
                report.overallCondition(),
                report.predictiveRisks().size(),
                Instant.now()
        ));

        return report;
    }
}
