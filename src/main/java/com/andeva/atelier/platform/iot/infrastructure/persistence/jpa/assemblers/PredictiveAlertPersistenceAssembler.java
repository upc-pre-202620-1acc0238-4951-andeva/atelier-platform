package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.PredictiveAlertPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Assembler for bidirectional translation between pure domain {@link PredictiveAlert}
 * aggregates and JPA {@link PredictiveAlertPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class PredictiveAlertPersistenceAssembler {

    public PredictiveAlertPersistenceEntity toEntity(PredictiveAlert domain) {
        if (domain == null) {
            return null;
        }
        var entity = new PredictiveAlertPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setVehicleId(domain.getVehicleId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setRecommendedServiceId(domain.getRecommendedServiceId().map(ServiceId::value).orElse(null));
        entity.setAlertType(domain.getAlertType());
        entity.setConfidenceScore(domain.getConfidenceScore().value());
        entity.setMessage(domain.getMessage());
        entity.setStatus(domain.getStatus());
        entity.setFcmMessageId(domain.getFcmMessageId().orElse(null));
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public PredictiveAlert toDomain(PredictiveAlertPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PredictiveAlert(
                new AlertId(entity.getId()),
                new VehicleId(entity.getVehicleId()),
                new TenantId(entity.getTenantId()),
                Optional.ofNullable(entity.getRecommendedServiceId()).map(ServiceId::new),
                entity.getAlertType(),
                new ConfidenceScore(entity.getConfidenceScore()),
                entity.getMessage(),
                entity.getStatus(),
                Optional.ofNullable(entity.getFcmMessageId()),
                entity.getCreatedAt()
        );
    }
}
