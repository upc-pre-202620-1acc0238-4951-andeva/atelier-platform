package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.events.TaskProposalSubmittedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderIntakeImageAttachedEvent;
import com.andeva.atelier.platform.operations.domain.model.events.WorkOrderTaskEvidenceAttachedEvent;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("A photo retried from the offline queue is attached once (R4)")
class WorkOrderPhotoIdempotencyTest {

    private static final StorageUrl PHOTO = StorageUrl.of(
            "https://demo.supabase.co/storage/v1/object/public/atelier/t/work-orders/w/photo-1.jpg");

    private WorkOrder order() {
        return WorkOrder.create(TenantId.of(UUID.randomUUID()), BranchId.of(UUID.randomUUID()), null,
                VehicleId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()), WorkOrderNumber.of(3),
                Mileage.of(1000), DiagnosticSummary.of("Ruido en suspensión"));
    }

    @Test
    @DisplayName("The same intake photo is stored once and announced once")
    void shouldAttachIntakeImageOnce() {
        WorkOrder order = order();

        order.attachIntakeImage(PHOTO, "Golpe en guardafango");
        order.attachIntakeImage(PHOTO, "Golpe en guardafango");

        assertThat(order.getIntakeImages()).hasSize(1);
        assertThat(order.domainEvents()).filteredOn(e -> e instanceof WorkOrderIntakeImageAttachedEvent).hasSize(1);
        assertThat(order.findIntakeImage(PHOTO)).isPresent();
    }

    @Test
    @DisplayName("The same task evidence is stored once; a different photo is added")
    void shouldAttachTaskEvidenceOnce() {
        WorkOrder order = order();
        WorkOrderTask task = order.addTask(ServiceId.of(UUID.randomUUID()), UUID.randomUUID(), "Amortiguadores",
                Money.soles(new BigDecimal("120.00")), LaborHours.of(BigDecimal.ONE));
        StorageUrl other = StorageUrl.of(PHOTO.value().replace("photo-1", "photo-2"));

        order.attachTaskEvidence(task.getId(), PHOTO, EvidenceType.DEFECT, "Desmontaje");
        order.attachTaskEvidence(task.getId(), PHOTO, EvidenceType.DEFECT, "Desmontaje");
        order.attachTaskEvidence(task.getId(), other, EvidenceType.COMPLETED, "Montaje");

        assertThat(task.getTaskImages()).hasSize(2);
        assertThat(order.domainEvents()).filteredOn(e -> e instanceof WorkOrderTaskEvidenceAttachedEvent).hasSize(2);
    }

    @Test
    @DisplayName("Resubmitting a proposal with the same photo returns the proposal already filed")
    void shouldSubmitProposalOnce() {
        WorkOrder order = order();
        UUID mechanic = UUID.randomUUID();

        TaskProposal first = order.submitProposal(mechanic, "Rótula con juego", ProposalSeverity.MEDIUM, PHOTO, null);
        TaskProposal retried = order.submitProposal(mechanic, "Rótula con juego", ProposalSeverity.MEDIUM, PHOTO, null);

        assertThat(retried.getId()).isEqualTo(first.getId());
        assertThat(order.getProposals()).hasSize(1);
        assertThat(order.domainEvents()).filteredOn(e -> e instanceof TaskProposalSubmittedEvent).hasSize(1);
    }
}
