package com.andeva.atelier.platform.hr.application.internal.queryservices;

import com.andeva.atelier.platform.hr.application.queryservices.EmployeeProfileQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByMembershipIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListEmployeeProfilesByBranchQuery;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeeProfileQueryServiceImpl implements EmployeeProfileQueryService {

    private final EmployeeProfileRepository employeeRepository;

    public EmployeeProfileQueryServiceImpl(EmployeeProfileRepository employeeRepository) {
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository cannot be null");
    }

    @Override
    public Optional<EmployeeProfile> handle(GetEmployeeProfileByIdQuery query) {
        return employeeRepository.findById(query.profileId())
                .filter(e -> e.getTenantId().equals(query.tenantId()));
    }

    @Override
    public Optional<EmployeeProfile> handle(GetEmployeeProfileByMembershipIdQuery query) {
        return employeeRepository.findByMembershipId(query.membershipId())
                .filter(e -> e.getTenantId().equals(query.tenantId()));
    }

    @Override
    public List<EmployeeProfile> handle(ListEmployeeProfilesByBranchQuery query) {
        return employeeRepository.findAllByBranchId(query.branchId()).stream()
                .filter(e -> e.getTenantId().equals(query.tenantId()))
                .toList();
    }
}
