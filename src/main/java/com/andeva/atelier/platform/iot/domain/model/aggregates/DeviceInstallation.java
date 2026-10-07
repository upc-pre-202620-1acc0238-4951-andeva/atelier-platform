package com.andeva.atelier.platform.iot.domain.model.aggregates;

import com.andeva.atelier.platform.iot.domain.model.events.DeviceInstalledOnVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.events.DeviceUninstalledFromVehicleEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root governing the physical mounting and active telemetry monitoring session
 * of an OBD-II scanner device coupled to a specific automotive vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public class DeviceInstallation extends AbstractDomainAggregateRoot<DeviceInstallation> {

    private final InstallationId id;
    private final DeviceId deviceId;
    private final VehicleId vehicleId;
    private final TenantId tenantId;
    private final Instant installedAt;
    private Instant uninstalledAt;
    private final int initialOdometerKm;
    private Integer finalOdometerKm;

    /**
     * Rehydration constructor for persistence assemblers.
     */
    public DeviceInstallation(
            InstallationId id,
            DeviceId deviceId,
            VehicleId vehicleId,
            TenantId tenantId,
            Instant installedAt,
            Optional<Instant> uninstalledAt,
            int initialOdometerKm,
            Optional<Integer> finalOdometerKm
    ) {
        this.id = Objects.requireNonNull(id, "InstallationId cannot be null");
        this.deviceId = Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        this.vehicleId = Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.installedAt = Objects.requireNonNull(installedAt, "installedAt cannot be null");
        this.uninstalledAt = uninstalledAt != null ? uninstalledAt.orElse(null) : null;
        if (initialOdometerKm < 0) {
            throw new IllegalArgumentException("Initial odometer reading cannot be negative: " + initialOdometerKm);
        }
        this.initialOdometerKm = initialOdometerKm;
        this.finalOdometerKm = finalOdometerKm != null ? finalOdometerKm.orElse(null) : null;

        if (this.uninstalledAt != null && this.uninstalledAt.isBefore(this.installedAt)) {
            throw new IllegalArgumentException("Uninstalled timestamp cannot be chronologically prior to installed timestamp");
        }
        if (this.finalOdometerKm != null && this.finalOdometerKm < this.initialOdometerKm) {
            throw new IllegalArgumentException(
                    String.format("Final odometer (%d km) cannot be less than initial odometer (%d km)",
                            this.finalOdometerKm, this.initialOdometerKm)
            );
        }
    }

    /**
     * Domain factory method initializing an active vehicle hardware monitoring session.
     */
    public static DeviceInstallation install(
            DeviceId deviceId,
            VehicleId vehicleId,
            TenantId tenantId,
            int currentOdometerKm
    ) {
        InstallationId installationId = InstallationId.generate();
        DeviceInstallation installation = new DeviceInstallation(
                installationId,
                deviceId,
                vehicleId,
                tenantId,
                Instant.now(),
                Optional.empty(),
                currentOdometerKm,
                Optional.empty()
        );
        installation.registerDomainEvent(DeviceInstalledOnVehicleEvent.of(installationId, deviceId, vehicleId, tenantId));
        return installation;
    }

    /**
     * Finalizes the monitoring session upon physical removal of the OBD-II scanner.
     */
    public void uninstall(int finalOdometerKm, Instant uninstalledTimestamp) {
        if (!isActive()) {
            throw new IllegalStateException("Device installation is already terminated.");
        }
        Objects.requireNonNull(uninstalledTimestamp, "Uninstalled timestamp cannot be null");
        if (uninstalledTimestamp.isBefore(this.installedAt)) {
            throw new IllegalArgumentException("Uninstallation date cannot be prior to installation date");
        }
        if (finalOdometerKm < this.initialOdometerKm) {
            throw new IllegalArgumentException(
                    String.format("Final odometer (%d km) cannot be less than initial odometer (%d km)",
                            finalOdometerKm, this.initialOdometerKm)
            );
        }
        this.uninstalledAt = uninstalledTimestamp;
        this.finalOdometerKm = finalOdometerKm;
        registerDomainEvent(DeviceUninstalledFromVehicleEvent.of(this.id, this.vehicleId, finalOdometerKm));
    }

    public boolean isActive() {
        return this.uninstalledAt == null;
    }

    public InstallationId getId() {
        return id;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public Instant getInstalledAt() {
        return installedAt;
    }

    public Optional<Instant> getUninstalledAt() {
        return Optional.ofNullable(uninstalledAt);
    }

    public int getInitialOdometerKm() {
        return initialOdometerKm;
    }

    public Optional<Integer> getFinalOdometerKm() {
        return Optional.ofNullable(finalOdometerKm);
    }
}
