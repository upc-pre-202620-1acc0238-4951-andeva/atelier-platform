package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.EmployeeProfilePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

@Component
public class EmployeeProfilePersistenceAssembler {

    public EmployeeProfile toDomain(EmployeeProfilePersistenceEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.valueOf(entity.getCurrency().toUpperCase());
        Money baseSalary = Money.of(entity.getBaseSalary(), currency);
        WorkShiftId shiftId = entity.getAssignedShiftId() != null ? WorkShiftId.of(entity.getAssignedShiftId()) : null;

        return new EmployeeProfile(
                EmployeeProfileId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                TenantMembershipId.of(entity.getMembershipId()),
                shiftId,
                baseSalary,
                entity.getCompensationType(),
                entity.getJobTitle(),
                entity.getEmploymentStatus()
        );
    }

    public EmployeeProfilePersistenceEntity toEntity(EmployeeProfile domain) {
        if (domain == null) return null;

        EmployeeProfilePersistenceEntity entity = new EmployeeProfilePersistenceEntity(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setBranchId(domain.getBranchId().value());
        entity.setMembershipId(domain.getMembershipId().value());
        entity.setAssignedShiftId(domain.getAssignedShiftId() != null ? domain.getAssignedShiftId().value() : null);
        entity.setBaseSalary(domain.getBaseSalary().amount());
        entity.setCurrency(domain.getBaseSalary().currency().name());
        entity.setCompensationType(domain.getCompensationType());
        entity.setJobTitle(domain.getJobTitle());
        entity.setEmploymentStatus(domain.getEmploymentStatus());

        return entity;
    }

    public void updateEntity(EmployeeProfilePersistenceEntity entity, EmployeeProfile domain) {
        entity.setBranchId(domain.getBranchId().value());
        entity.setAssignedShiftId(domain.getAssignedShiftId() != null ? domain.getAssignedShiftId().value() : null);
        entity.setBaseSalary(domain.getBaseSalary().amount());
        entity.setCurrency(domain.getBaseSalary().currency().name());
        entity.setCompensationType(domain.getCompensationType());
        entity.setJobTitle(domain.getJobTitle());
        entity.setEmploymentStatus(domain.getEmploymentStatus());
    }
}
