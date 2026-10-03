package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for managing Invitation onboarding aggregates.
 *
 * @author Joel Huamani Estefanero
 */
public interface InvitationRepository {

    Invitation save(Invitation invitation);

    Optional<Invitation> findById(InvitationId id);

    Optional<Invitation> findByToken(String token);

    Optional<Invitation> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email);

    List<Invitation> findByTenantId(TenantId tenantId);
}
