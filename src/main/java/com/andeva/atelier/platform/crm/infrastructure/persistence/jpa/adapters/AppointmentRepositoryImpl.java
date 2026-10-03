package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers.AppointmentPersistenceAssembler;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.AppointmentPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA adapter implementing the AppointmentRepository domain port.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class AppointmentRepositoryImpl implements AppointmentRepository {

    private final AppointmentPersistenceRepository appointmentPersistenceRepository;

    public AppointmentRepositoryImpl(AppointmentPersistenceRepository appointmentPersistenceRepository) {
        this.appointmentPersistenceRepository = Objects.requireNonNull(appointmentPersistenceRepository);
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentPersistenceEntity entity = AppointmentPersistenceAssembler.toEntity(appointment);
        AppointmentPersistenceEntity saved = appointmentPersistenceRepository.save(entity);
        return AppointmentPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Appointment> findById(AppointmentId id) {
        return appointmentPersistenceRepository.findById(id.value())
                .map(AppointmentPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Appointment> findByIdAndTenantId(AppointmentId id, TenantId tenantId) {
        return appointmentPersistenceRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(AppointmentPersistenceAssembler::toDomain);
    }

    @Override
    public List<Appointment> findByTenantIdAndBranchIdAndDate(TenantId tenantId, BranchId branchId, Instant startOfDay, Instant endOfDay) {
        return appointmentPersistenceRepository.findAllByTenantIdAndBranchIdAndDate(
                tenantId.value(),
                branchId.value(),
                startOfDay,
                endOfDay
        ).stream()
         .map(AppointmentPersistenceAssembler::toDomain)
         .toList();
    }

    @Override
    public List<Appointment> findByCustomerId(CustomerId customerId) {
        return appointmentPersistenceRepository.findByCustomerIdOrderByScheduledAtDesc(customerId.value())
                .stream()
                .map(AppointmentPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findByVehicleId(VehicleId vehicleId) {
        return appointmentPersistenceRepository.findByVehicleIdOrderByScheduledAtDesc(vehicleId.value())
                .stream()
                .map(AppointmentPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Appointment> findActiveByBranchInWindow(TenantId tenantId, BranchId branchId, Instant windowStart, Instant windowEnd) {
        return appointmentPersistenceRepository.findActiveByBranchInWindow(
                tenantId.value(),
                branchId.value(),
                windowStart,
                windowEnd
        ).stream()
         .map(AppointmentPersistenceAssembler::toDomain)
         .toList();
    }

    @Override
    public boolean existsActiveAppointmentsByCustomerId(CustomerId customerId) {
        return appointmentPersistenceRepository.existsActiveAppointmentsByCustomerId(customerId.value());
    }
}
