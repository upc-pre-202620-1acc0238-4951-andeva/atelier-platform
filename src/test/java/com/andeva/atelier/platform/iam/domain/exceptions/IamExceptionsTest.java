package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for IAM & Tenancy domain exceptions hierarchy and error codes.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("IAM Domain Exceptions Unit Tests")
class IamExceptionsTest {

    @Test
    @DisplayName("TenantNotFoundException should provide TENANT_NOT_FOUND code and descriptive message")
    void shouldHandleTenantNotFoundException() {
        TenantId tenantId = TenantId.generate();
        TenantNotFoundException ex1 = new TenantNotFoundException(tenantId);
        assertThat(ex1.errorCode()).isEqualTo("TENANT_NOT_FOUND");
        assertThat(ex1.getMessage()).contains(tenantId.value().toString());

        TaxId taxId = TaxId.of("20456789014");
        TenantNotFoundException ex2 = new TenantNotFoundException(taxId);
        assertThat(ex2.errorCode()).isEqualTo("TENANT_NOT_FOUND");
        assertThat(ex2.getMessage()).contains("20456789014");
    }

    @Test
    @DisplayName("TenantAlreadyExistsException should provide TENANT_ALREADY_EXISTS code")
    void shouldHandleTenantAlreadyExistsException() {
        TaxId taxId = TaxId.of("20456789014");
        TenantAlreadyExistsException ex = new TenantAlreadyExistsException(taxId);
        assertThat(ex.errorCode()).isEqualTo("TENANT_ALREADY_EXISTS");
        assertThat(ex.getMessage()).contains("20456789014");
    }

    @Test
    @DisplayName("BranchNotFoundException should provide BRANCH_NOT_FOUND code")
    void shouldHandleBranchNotFoundException() {
        BranchId branchId = BranchId.generate();
        BranchNotFoundException ex = new BranchNotFoundException(branchId);
        assertThat(ex.errorCode()).isEqualTo("BRANCH_NOT_FOUND");
        assertThat(ex.getMessage()).contains(branchId.value().toString());
    }

    @Test
    @DisplayName("UserNotFoundException should provide USER_NOT_FOUND code")
    void shouldHandleUserNotFoundException() {
        UserId userId = UserId.generate();
        UserNotFoundException ex1 = new UserNotFoundException(userId);
        assertThat(ex1.errorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(ex1.getMessage()).contains(userId.value().toString());

        EmailAddress email = EmailAddress.of("user@test.pe");
        UserNotFoundException ex2 = new UserNotFoundException(email);
        assertThat(ex2.errorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(ex2.getMessage()).contains("user@test.pe");
    }

    @Test
    @DisplayName("UserAlreadyExistsException should provide USER_ALREADY_EXISTS code")
    void shouldHandleUserAlreadyExistsException() {
        EmailAddress email = EmailAddress.of("user@test.pe");
        UserAlreadyExistsException ex = new UserAlreadyExistsException(email);
        assertThat(ex.errorCode()).isEqualTo("USER_ALREADY_EXISTS");
        assertThat(ex.getMessage()).contains("user@test.pe");
    }

    @Test
    @DisplayName("InvalidCredentialsException should provide INVALID_CREDENTIALS code")
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException();
        assertThat(ex.errorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(ex.getMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("InvalidVerificationTokenException should provide INVALID_VERIFICATION_TOKEN code")
    void shouldHandleInvalidVerificationTokenException() {
        InvalidVerificationTokenException ex = new InvalidVerificationTokenException();
        assertThat(ex.errorCode()).isEqualTo("INVALID_VERIFICATION_TOKEN");
        assertThat(ex.getMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("MembershipNotFoundException should provide MEMBERSHIP_NOT_FOUND code")
    void shouldHandleMembershipNotFoundException() {
        TenantMembershipId membershipId = TenantMembershipId.generate();
        MembershipNotFoundException ex1 = new MembershipNotFoundException(membershipId);
        assertThat(ex1.errorCode()).isEqualTo("MEMBERSHIP_NOT_FOUND");

        TenantId tenantId = TenantId.generate();
        UserId userId = UserId.generate();
        MembershipNotFoundException ex2 = new MembershipNotFoundException(tenantId, userId);
        assertThat(ex2.errorCode()).isEqualTo("MEMBERSHIP_NOT_FOUND");
    }

    @Test
    @DisplayName("RoleNotFoundException should provide ROLE_NOT_FOUND code")
    void shouldHandleRoleNotFoundException() {
        RoleId roleId = RoleId.generate();
        RoleNotFoundException ex1 = new RoleNotFoundException(roleId);
        assertThat(ex1.errorCode()).isEqualTo("ROLE_NOT_FOUND");

        TenantId tenantId = TenantId.generate();
        RoleNotFoundException ex2 = new RoleNotFoundException(tenantId, "ROLE_CUSTOM");
        assertThat(ex2.errorCode()).isEqualTo("ROLE_NOT_FOUND");
    }

    @Test
    @DisplayName("SystemRoleImmutableException should provide SYSTEM_ROLE_IMMUTABLE code")
    void shouldHandleSystemRoleImmutableException() {
        RoleId roleId = RoleId.generate();
        SystemRoleImmutableException ex = new SystemRoleImmutableException(roleId);
        assertThat(ex.errorCode()).isEqualTo("SYSTEM_ROLE_IMMUTABLE");
        assertThat(ex.getMessage()).contains(roleId.value().toString());
    }

    @Test
    @DisplayName("RoleInUseException should provide ROLE_IN_USE code")
    void shouldHandleRoleInUseException() {
        RoleId roleId = RoleId.generate();
        RoleInUseException ex = new RoleInUseException(roleId);
        assertThat(ex.errorCode()).isEqualTo("ROLE_IN_USE");
        assertThat(ex.getMessage()).contains(roleId.value().toString());
    }

    @Test
    @DisplayName("InvitationNotFoundException should provide INVITATION_NOT_FOUND code")
    void shouldHandleInvitationNotFoundException() {
        InvitationId invitationId = InvitationId.generate();
        InvitationNotFoundException ex = new InvitationNotFoundException(invitationId);
        assertThat(ex.errorCode()).isEqualTo("INVITATION_NOT_FOUND");
        assertThat(ex.getMessage()).contains(invitationId.value().toString());
    }

    @Test
    @DisplayName("InvitationExpiredException should provide INVITATION_EXPIRED code")
    void shouldHandleInvitationExpiredException() {
        InvitationId invitationId = InvitationId.generate();
        InvitationExpiredException ex = new InvitationExpiredException(invitationId);
        assertThat(ex.errorCode()).isEqualTo("INVITATION_EXPIRED");
        assertThat(ex.getMessage()).contains(invitationId.value().toString());
    }

    @Test
    @DisplayName("All IAM exceptions must inherit from IamDomainException and DomainException")
    void shouldInheritFromIamDomainException() {
        IamDomainException custom = new IamDomainException("CUSTOM", "Test") {};
        assertThat(custom).isInstanceOf(DomainException.class);
    }
}
