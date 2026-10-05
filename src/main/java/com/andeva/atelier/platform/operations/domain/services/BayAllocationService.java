package com.andeva.atelier.platform.operations.domain.services;

import com.andeva.atelier.platform.operations.domain.exceptions.WorkBayNotFoundException;
import com.andeva.atelier.platform.operations.domain.exceptions.WorkBayOccupiedException;
import com.andeva.atelier.platform.operations.domain.exceptions.WorkBayUnderMaintenanceException;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.enums.BayStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.repositories.WorkBayRepository;
import org.springframework.stereotype.Service;

@Service
public class BayAllocationService {

    private final WorkBayRepository workBayRepository;

    public BayAllocationService(WorkBayRepository workBayRepository) {
        this.workBayRepository = workBayRepository;
    }

    public void validateBayAvailability(WorkBayId bayId, WorkOrderId orderId) {
        WorkBay bay = workBayRepository.findById(bayId)
                .orElseThrow(() -> new WorkBayNotFoundException(bayId));

        if (bay.getStatus() == BayStatus.OCCUPIED) {
            throw new WorkBayOccupiedException(bayId);
        }

        if (bay.getStatus() == BayStatus.MAINTENANCE) {
            throw new WorkBayUnderMaintenanceException(bayId);
        }
    }
}
