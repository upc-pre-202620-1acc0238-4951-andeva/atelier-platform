package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceBrokenEvent;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceLostEvent;
import com.andeva.atelier.platform.iot.domain.model.events.Obd2DeviceRegisteredEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Aggregate Root representing a physical on-board diagnostics (OBD-II) telemetry scanner
 * owned by the automotive workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class Obd2Device extends AbstractDomainAggregateRoot<Obd2Device> {

    private final DeviceId id;
    private final TenantId tenantId;
    private final DeviceIdentifier deviceIdentifier;
    private final ConnectionType connectionType;
    private DeviceStatus status;
    private String hardwareModel;
    private String firmwareVersion;

    /**
     * Rehydration constructor for persistence assemblers.
     */
    public Obd2Device(
            DeviceId id,
            TenantId tenantId,
            DeviceIdentifier deviceIdentifier,
            ConnectionType connectionType,
            DeviceStatus status,
            String hardwareModel,
            String firmwareVersion
    ) {
        this.id = Objects.requireNonNull(id, "DeviceId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.deviceIdentifier = Objects.requireNonNull(deviceIdentifier, "DeviceIdentifier cannot be null");
        this.connectionType = Objects.requireNonNull(connectionType, "ConnectionType cannot be null");
        this.status = Objects.requireNonNull(status, "DeviceStatus cannot be null");
        this.hardwareModel = hardwareModel != null ? hardwareModel : "Generic OBD-II";
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "1.0.0";
    }

    /**
     * Domain factory method for onboarding a new OBD-II device into the tenant inventory.
     */
    public static Obd2Device register(
            TenantId tenantId,
            DeviceIdentifier identifier,
            ConnectionType type,
            String model,
            String firmware
    ) {
        DeviceId deviceId = DeviceId.generate();
        Obd2Device device = new Obd2Device(
                deviceId,
                tenantId,
                identifier,
                type,
                DeviceStatus.ACTIVE,
                model,
                firmware
        );
        device.registerDomainEvent(Obd2DeviceRegisteredEvent.of(deviceId, tenantId, identifier));
        return device;
    }

    public void markLost() {
        this.status = DeviceStatus.LOST;
        registerDomainEvent(Obd2DeviceLostEvent.of(this.id, this.tenantId));
    }

    public void markBroken() {
        this.status = DeviceStatus.BROKEN;
        registerDomainEvent(Obd2DeviceBrokenEvent.of(this.id, this.tenantId));
    }

    public void markActive() {
        this.status = DeviceStatus.ACTIVE;
    }

    public void markInactive() {
        this.status = DeviceStatus.INACTIVE;
    }

    public void updateFirmware(String newVersion) {
        Objects.requireNonNull(newVersion, "Firmware version cannot be null");
        this.firmwareVersion = newVersion.trim();
    }

    public boolean isOperational() {
        return this.status.isOperational();
    }

    public DeviceId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public String getHardwareModel() {
        return hardwareModel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }
}
