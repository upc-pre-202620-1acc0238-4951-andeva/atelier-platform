package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleHealthReport;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.ids.HealthReportId;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleHealthReportRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.VehicleHealthReportPersistenceEntity;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.VehicleHealthReportPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for issued AI vehicle health reports; the structured diagnosis is
 * serialized to JSON so the report is reproduced exactly as it was issued.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class VehicleHealthReportRepositoryAdapter implements VehicleHealthReportRepository {

    private final VehicleHealthReportPersistenceRepository persistenceRepository;
    private final ObjectMapper objectMapper;

    public VehicleHealthReportRepositoryAdapter(
            VehicleHealthReportPersistenceRepository persistenceRepository,
            ObjectMapper objectMapper
    ) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
    }

    @Override
    public VehicleHealthReport save(VehicleHealthReport report) {
        Objects.requireNonNull(report, "report cannot be null");
        return toDomain(persistenceRepository.save(toEntity(report)));
    }

    @Override
    public Optional<VehicleHealthReport> findById(HealthReportId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<VehicleHealthReport> findLatestByTenantIdAndVehicleId(TenantId tenantId, VehicleId vehicleId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(vehicleId, "vehicleId cannot be null");
        return persistenceRepository.findFirstByTenantIdAndVehicleIdOrderByGeneratedAtDesc(tenantId.value(), vehicleId.value())
                .map(this::toDomain);
    }

    @Override
    public List<VehicleHealthReport> findLatestByTenantIdAndVehicleIds(TenantId tenantId, List<VehicleId> vehicleIds) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = vehicleIds.stream().map(VehicleId::value).toList();
        // Rows arrive newest first: the first one seen per vehicle is its latest report
        Map<UUID, VehicleHealthReportPersistenceEntity> latest = new LinkedHashMap<>();
        persistenceRepository.findAllByTenantIdAndVehicleIdInOrderByGeneratedAtDesc(tenantId.value(), ids)
                .forEach(entity -> latest.putIfAbsent(entity.getVehicleId(), entity));
        return latest.values().stream().map(this::toDomain).toList();
    }

    @Override
    public long countByTenantIdGeneratedSince(TenantId tenantId, Instant since) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(since, "since cannot be null");
        return persistenceRepository.countByTenantIdAndGeneratedAtGreaterThanEqual(tenantId.value(), since);
    }

    private VehicleHealthReportPersistenceEntity toEntity(VehicleHealthReport report) {
        VehicleHealthReportPersistenceEntity entity = new VehicleHealthReportPersistenceEntity(report.getId().value());
        entity.setTenantId(report.getTenantId().value());
        entity.setVehicleId(report.getVehicleId().value());
        entity.setDaysAnalyzed(report.getDaysAnalyzed());
        entity.setHealthScore(report.getDiagnosis().healthScore());
        entity.setOverallCondition(report.getDiagnosis().overallCondition());
        entity.setGeneratedAt(report.getGeneratedAt());
        try {
            entity.setDiagnosis(objectMapper.writeValueAsString(report.getDiagnosis()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize the vehicle health diagnosis", e);
        }
        return entity;
    }

    private VehicleHealthReport toDomain(VehicleHealthReportPersistenceEntity entity) {
        VehicleHealthReportAiDto diagnosis;
        try {
            diagnosis = objectMapper.readValue(entity.getDiagnosis(), VehicleHealthReportAiDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored vehicle health diagnosis is unreadable: " + entity.getId(), e);
        }
        return new VehicleHealthReport(
                HealthReportId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                VehicleId.of(entity.getVehicleId()),
                entity.getDaysAnalyzed(),
                diagnosis,
                entity.getGeneratedAt()
        );
    }
}
