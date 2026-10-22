package com.andeva.atelier.platform.inventory.infrastructure.external.notification;

import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.inventory.application.internal.outbound.acl.InventoryStaffNotificationPort;
import com.andeva.atelier.platform.shared.infrastructure.messaging.firebase.FcmPushSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Delivers inventory notifications as FCM pushes to the device registered by each user.
 */
@Component
public class InventoryStaffFcmNotificationAdapter implements InventoryStaffNotificationPort {

    private final TenancyContextFacade tenancyContextFacade;
    private final FcmPushSender pushSender;

    public InventoryStaffFcmNotificationAdapter(TenancyContextFacade tenancyContextFacade, FcmPushSender pushSender) {
        this.tenancyContextFacade = Objects.requireNonNull(tenancyContextFacade, "tenancyContextFacade cannot be null");
        this.pushSender = Objects.requireNonNull(pushSender, "pushSender cannot be null");
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
