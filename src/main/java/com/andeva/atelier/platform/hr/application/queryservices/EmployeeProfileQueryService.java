package com.andeva.atelier.platform.hr.application.queryservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByMembershipIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListEmployeeProfilesByBranchQuery;

import java.util.List;
import java.util.Optional;

public interface EmployeeProfileQueryService {
    Optional<EmployeeProfile> handle(GetEmployeeProfileByIdQuery query);
    Optional<EmployeeProfile> handle(GetEmployeeProfileByMembershipIdQuery query);
    List<EmployeeProfile> handle(ListEmployeeProfilesByBranchQuery query);
}
