package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.BranchCommandServiceImpl;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateBranchCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.repositories.BranchRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link BranchCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Branch Command Service Unit Tests")
class BranchCommandServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private SubscriptionQuotaPort subscriptionQuotaPort;

    private BranchCommandService branchCommandService;

    private final TenantId tenantId = TenantId.generate();
    private final TaxId taxId = TaxId.ruc("20456789014");
    private final GeoPoint location = GeoPoint.of(-12.0855, -77.0345);

    @BeforeEach
    void setUp() {
        branchCommandService = new BranchCommandServiceImpl(
                tenantRepository,
                branchRepository,
                subscriptionQuotaPort
        );
    }

    @Test
    @DisplayName("Should create branch and validate quota successfully")
    void shouldCreateBranchSuccessfully() {
        Tenant tenant = Tenant.create("AutoTaller", "AutoTaller S.A.", taxId);
        CreateBranchCommand command = new CreateBranchCommand(
                tenant.id(), "Sede Surco", "0001", location, 300
        );

        when(tenantRepository.findById(tenant.id())).thenReturn(Optional.of(tenant));
        doNothing().when(subscriptionQuotaPort).validateBranchCreationAllowed(eq(tenant.id()), anyInt());
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Branch branch = result.getOrThrow();
        assertThat(branch.name()).isEqualTo("Sede Surco");
        assertThat(branch.sunatCode()).isEqualTo("0001");
        assertThat(branch.location()).isEqualTo(location);
        assertThat(branch.geofenceRadiusMeters()).isEqualTo(300);

        verify(subscriptionQuotaPort).validateBranchCreationAllowed(eq(tenant.id()), eq(0));
        verify(tenantRepository).save(tenant);
        verify(branchRepository).save(branch);
    }

    @Test
    @DisplayName("Should reject branch creation when Tenant is not found")
    void shouldRejectWhenTenantNotFound() {
        CreateBranchCommand command = new CreateBranchCommand(
                tenantId, "Sede Surco", "0001", location, 300
        );
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject branch creation when subscription quota is exceeded")
    void shouldRejectWhenQuotaExceeded() {
        Tenant tenant = Tenant.create("AutoTaller", "AutoTaller S.A.", taxId);
        CreateBranchCommand command = new CreateBranchCommand(
                tenant.id(), "Sede Surco", "0001", location, 300
        );

        when(tenantRepository.findById(tenant.id())).thenReturn(Optional.of(tenant));
        doThrow(new IllegalStateException("Branch limit reached for plan"))
                .when(subscriptionQuotaPort).validateBranchCreationAllowed(eq(tenant.id()), anyInt());

        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("FORBIDDEN");
        assertThat(result.getError().message()).contains("Branch limit reached");
        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update branch location and geofence radius successfully")
    void shouldUpdateBranchLocationSuccessfully() {
        Branch branch = Branch.create(tenantId, "Sede Miraflores", "0002", location, 200);
        GeoPoint newLocation = GeoPoint.of(-12.1200, -77.0280);
        UpdateBranchLocationCommand command = new UpdateBranchLocationCommand(
                branch.id(), "Sede Miraflores Renovada", "0002", newLocation, 450
        );

        when(branchRepository.findById(branch.id())).thenReturn(Optional.of(branch));
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Branch updated = result.getOrThrow();
        assertThat(updated.name()).isEqualTo("Sede Miraflores Renovada");
        assertThat(updated.location()).isEqualTo(newLocation);
        assertThat(updated.geofenceRadiusMeters()).isEqualTo(450);
        verify(branchRepository).save(branch);
    }

    @Test
    @DisplayName("Should reject branch location update when branch is not found")
    void shouldRejectUpdateWhenBranchNotFound() {
        BranchId branchId = BranchId.generate();
        UpdateBranchLocationCommand command = new UpdateBranchLocationCommand(
                branchId, "Sede Miraflores", "0002", location, 450
        );
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        Result<Branch, ApplicationError> result = branchCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
    }
}
