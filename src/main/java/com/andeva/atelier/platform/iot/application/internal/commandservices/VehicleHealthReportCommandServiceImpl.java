package com.andeva.atelier.platform.iot.application.internal.commandservices;

import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.AiInferenceDiagnosticPort;
import com.andeva.atelier.platform.iot.application.internal.outbound.acl.IoTSubscriptionQuotaPort;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleHealthReportRepository;
import com.andeva.atelier.platform.iot.interfaces.events.VehicleHealthReportGeneratedIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Command Service implementation orchestrating predictive vehicle health evaluations via Spring AI and Groq LPU.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class VehicleHealthReportCommandServiceImpl implements VehicleHealthReportCommandService {

    // Monthly AI report quotas follow the workshop's calendar month in Peru
    private static final ZoneId QUOTA_ZONE = ZoneId.of("America/Lima");

    private final IoTSubscriptionQuotaPort subscriptionQuotaPort;
    private final AiInferenceDiagnosticPort aiInferenceDiagnosticPort;
    private final VehicleHealthReportRepository reportRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VehicleHealthReportCommandServiceImpl(
            IoTSubscriptionQuotaPort subscriptionQuotaPort,
            AiInferenceDiagnosticPort aiInferenceDiagnosticPort,
            VehicleHealthReportRepository reportRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "IoTSubscriptionQuotaPort cannot be null");
        this.aiInferenceDiagnosticPort = Objects.requireNonNull(aiInferenceDiagnosticPort, "AiInferenceDiagnosticPort cannot be null");
        this.reportRepository = Objects.requireNonNull(reportRepository, "VehicleHealthReportRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher cannot be null");
    }

    @Override
    public VehicleHealthReport handle(GenerateVehicleHealthReportCommand command) {
        Objects.requireNonNull(command, "GenerateVehicleHealthReportCommand cannot be null");

        // 1. Enforce SaaS subscription plan quota for monthly predictive AI reports
        Instant monthStart = LocalDate.now(QUOTA_ZONE).withDayOfMonth(1).atStartOfDay(QUOTA_ZONE).toInstant();
        long reportsThisMonth = reportRepository.countByTenantIdGeneratedSince(command.tenantId(), monthStart);
        subscriptionQuotaPort.validateAiReportGenerationAllowed(command.tenantId(), (int) reportsThisMonth);

        // 2. Perform deep multi-signal Groq LPU diagnostic evaluation
        VehicleHealthReportAiDto diagnosis = aiInferenceDiagnosticPort.generateVehicleDiagnostic(
                command.tenantId(),
                command.vehicleId(),
                command.daysToAnalyze(),
                command.includeResolvedDtcHistory()
        );

        // 3. Persist the issued report so later reads and PDF exports serve this exact diagnosis
        VehicleHealthReport report = reportRepository.save(
                VehicleHealthReport.issue(command.tenantId(), command.vehicleId(), command.daysToAnalyze(), diagnosis));

        // 4. Publish integration event for cross-bounded-context awareness (CRM, MRO, Invoicing)
        eventPublisher.publishEvent(new VehicleHealthReportGeneratedIntegrationEvent(
                command.tenantId().value(),
                command.vehicleId().value(),
                diagnosis.healthScore(),
                diagnosis.overallCondition(),
                diagnosis.predictiveRisks().size(),
                Instant.now()
        ));

        return report;
    }
}
