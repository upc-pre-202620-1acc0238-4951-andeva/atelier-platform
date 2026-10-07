package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities;

import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.ConnectionTypeConverter;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.converters.DeviceStatusConverter;
import com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code obd2_devices} relational table.
 * Persists the physical hardware inventory of OBD-II telematics devices.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "obd2_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_obd2_devices_identifier", columnNames = {"device_identifier"})
        }
)
public class Obd2DevicePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "device_identifier", nullable = false, length = 100)
    private String deviceIdentifier;

    @Convert(converter = ConnectionTypeConverter.class)
    @Column(name = "connection_type", nullable = false, length = 20)
    private ConnectionType connectionType;

    @Convert(converter = DeviceStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "hardware_model", length = 100)
    private String hardwareModel;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    public Obd2DevicePersistenceEntity(UUID id) {
        super(id);
    }
}
