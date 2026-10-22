package com.andeva.atelier.platform.hr.infrastructure.external.notification;

import com.andeva.atelier.platform.hr.application.internal.outbound.acl.EmployeeNotificationPort;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.shared.infrastructure.messaging.firebase.FcmPushSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmployeeFcmNotificationAdapter implements EmployeeNotificationPort {

    private final TenancyContextFacade tenancyContextFacade;
    private final FcmPushSender pushSender;

    public EmployeeFcmNotificationAdapter(TenancyContextFacade tenancyContextFacade, FcmPushSender pushSender) {
        this.tenancyContextFacade = Objects.requireNonNull(tenancyContextFacade, "tenancyContextFacade cannot be null");
        this.pushSender = Objects.requireNonNull(pushSender, "pushSender cannot be null");
    }

    @Override
    public void notifyEmployee(UUID membershipId, String title, String body, Map<String, String> data) {
        tenancyContextFacade.fetchUserIdOfMembership(membershipId)
                .flatMap(tenancyContextFacade::fetchUserDeviceToken)
                .ifPresent(token -> pushSender.send(token, title, body, data));
    }

    @Override
    public void notifyPermissionHolders(UUID tenantId, String permission, String title, String body, Map<String, String> data) {
        tenancyContextFacade.fetchActiveUserIdsWithPermission(tenantId, permission).stream()
                .map(tenancyContextFacade::fetchUserDeviceToken)
                .flatMap(Optional::stream)
                .distinct()
                .forEach(token -> pushSender.send(token, title, body, data));
    }
}
