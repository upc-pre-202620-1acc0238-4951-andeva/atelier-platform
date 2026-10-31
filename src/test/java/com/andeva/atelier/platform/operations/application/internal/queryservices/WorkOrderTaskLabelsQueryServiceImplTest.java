package com.andeva.atelier.platform.operations.application.internal.queryservices;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import com.andeva.atelier.platform.operations.application.queryservices.WorkOrderTaskLabelsQueryService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("H11: catalog name of a service looked up by its id")
class WorkOrderTaskLabelsQueryServiceImplTest {

    private final ServiceRepository serviceRepository = mock(ServiceRepository.class);
    private final WorkOrderTaskLabelsQueryServiceImpl labels =
            new WorkOrderTaskLabelsQueryServiceImpl(serviceRepository, mock(InventoryReservationAclService.class));

    @Test
    @DisplayName("A catalog service answers its own name")
    void shouldAnswerCatalogName() {
        ServiceId serviceId = ServiceId.generate();
        Service service = mock(Service.class);
        when(service.getName()).thenReturn("Cambio de bomba de agua");
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        assertThat(labels.serviceNameOf(serviceId)).isEqualTo("Cambio de bomba de agua");
    }

    @Test
    @DisplayName("A missing or absent service falls back to the generic label")
    void shouldFallBackWithoutService() {
        ServiceId serviceId = ServiceId.generate();
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThat(labels.serviceNameOf(serviceId)).isEqualTo(WorkOrderTaskLabelsQueryService.SERVICE_FALLBACK);
        assertThat(labels.serviceNameOf((ServiceId) null)).isEqualTo(WorkOrderTaskLabelsQueryService.SERVICE_FALLBACK);
    }
}
