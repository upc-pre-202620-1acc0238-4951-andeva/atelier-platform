package com.andeva.atelier.platform.hr.application.internal.schedulers;

import com.andeva.atelier.platform.hr.application.internal.outbound.acl.EmployeeNotificationPort;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

/**
 * Reminds each employee before the start of the assigned shift, with the tolerated grace window (US09).
 * Runs every five minutes and notifies the shifts that start 15 to 20 minutes ahead, so each
 * shift is reminded exactly once per day.
 */
@Component
public class ShiftStartReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ShiftStartReminderScheduler.class);
    private static final ZoneId WORKSHOP_ZONE = ZoneId.of("America/Lima");
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");
    private static final int LEAD_MINUTES = 15;
    private static final int WINDOW_MINUTES = 5;

    private final WorkShiftRepository workShiftRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeNotificationPort notificationPort;

    public ShiftStartReminderScheduler(WorkShiftRepository workShiftRepository,
                                       EmployeeProfileRepository employeeProfileRepository,
                                       EmployeeNotificationPort notificationPort) {
        this.workShiftRepository = Objects.requireNonNull(workShiftRepository);
        this.employeeProfileRepository = Objects.requireNonNull(employeeProfileRepository);
        this.notificationPort = Objects.requireNonNull(notificationPort);
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "America/Lima")
    @Transactional(readOnly = true)
    public void remindUpcomingShifts() {
        remindShiftsStartingAfter(LocalTime.now(WORKSHOP_ZONE).truncatedTo(ChronoUnit.MINUTES));
    }

    /**
     * @return number of reminders sent
     */
    public int remindShiftsStartingAfter(LocalTime now) {
        return remindShiftsStartingAfter(now, java.time.LocalDate.now(WORKSHOP_ZONE).getDayOfWeek());
    }

    /**
     * @param today day of the week in the workshop zone: shifts resting that day send no reminder
     * @return number of reminders sent
     */
    public int remindShiftsStartingAfter(LocalTime now, java.time.DayOfWeek today) {
        LocalTime from = now.plusMinutes(LEAD_MINUTES);
        LocalTime to = from.plusMinutes(WINDOW_MINUTES);
        if (to.isBefore(from)) {
            return 0; // the window crosses midnight; such shifts are reminded on the next run
        }
        int sent = 0;
        for (WorkShift shift : workShiftRepository.findActiveStartingBetween(from, to)) {
            if (!shift.isWorkDay(today)) {
                continue;
            }
            String start = shift.getSchedule().startTime().format(HOUR);
            String body = "Tu turno " + shift.getName() + " inicia a las " + start + ". Tolerancia: "
                    + shift.getGracePeriod().minutes() + " minutos.";
            for (EmployeeProfile employee : employeeProfileRepository.findAllActiveByAssignedShiftId(shift.getId())) {
                try {
                    notificationPort.notifyEmployee(employee.getMembershipId().value(), "Tu turno inicia pronto", body,
                            Map.of("type", "SHIFT_REMINDER", "shiftId", shift.getId().value().toString(), "startTime", start));
                    sent++;
                } catch (RuntimeException e) {
                    log.warn("Shift reminder could not be delivered to membership {}: {}", employee.getMembershipId().value(), e.getMessage());
                }
            }
        }
        return sent;
    }
}
