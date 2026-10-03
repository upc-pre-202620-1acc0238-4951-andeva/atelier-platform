package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code vehicle_ownerships} relational table.
 *
 * @author Adiel Sanchez Santin
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vehicle_ownerships",
        indexes = {
                @Index(name = "idx_vo_vehicle_dates", columnList = "vehicle_id, start_date, end_date"),
                @Index(name = "idx_vo_customer_active", columnList = "customer_id, end_date"),
                @Index(name = "idx_vo_user_active", columnList = "user_id, end_date")
        }
)
public class VehicleOwnershipPersistenceEntity extends CrmAuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vo_vehicle"))
    private VehiclePersistenceEntity vehicle;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public VehicleOwnershipPersistenceEntity(UUID id) {
        super(id);
    }

    public VehicleOwnershipPersistenceEntity(
            UUID id,
            VehiclePersistenceEntity vehicle,
            UUID customerId,
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super(id);
        this.vehicle = vehicle;
        this.customerId = customerId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
