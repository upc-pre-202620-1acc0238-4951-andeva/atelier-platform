package com.andeva.atelier.platform.hr.application;

import com.andeva.atelier.platform.hr.application.internal.outbound.acl.EmployeeNotificationPort;
import com.andeva.atelier.platform.hr.application.internal.schedulers.ShiftStartReminderScheduler;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reminder before the start of the shift (US09)")
class ShiftStartReminderSchedulerTest {

    @Mock
    private WorkShiftRepository workShiftRepository;
    @Mock
    private EmployeeProfileRepository employeeProfileRepository;
    @Mock
    private EmployeeNotificationPort notificationPort;

    @Test
    @DisplayName("Reminds the employees of shifts starting 15 to 20 minutes later, with the grace window")
    void shouldRemindUpcomingShift() {
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        WorkShift morning = WorkShift.create(tenantId, "Mañana", LocalTime.of(8, 0), LocalTime.of(17, 0), 15);
        TenantMembershipId membership = TenantMembershipId.of(UUID.randomUUID());
        EmployeeProfile mechanic = new EmployeeProfile(EmployeeProfileId.generate(), tenantId, BranchId.of(UUID.randomUUID()),
                membership, morning.getId(), Money.soles(1500.0), CompensationType.values()[0], "Mecánico", EmploymentStatus.ACTIVE);

        when(workShiftRepository.findActiveStartingBetween(LocalTime.of(8, 0), LocalTime.of(8, 5))).thenReturn(List.of(morning));
        when(employeeProfileRepository.findAllActiveByAssignedShiftId(morning.getId())).thenReturn(List.of(mechanic));

        var scheduler = new ShiftStartReminderScheduler(workShiftRepository, employeeProfileRepository, notificationPort);

        assertThat(scheduler.remindShiftsStartingAfter(LocalTime.of(7, 45))).isEqualTo(1);
        verify(notificationPort).notifyEmployee(eq(membership.value()), eq("Tu turno inicia pronto"), contains("08:00"), anyMap());
    }
}
