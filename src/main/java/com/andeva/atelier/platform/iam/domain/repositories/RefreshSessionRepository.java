package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.RefreshSession;

import java.util.Optional;

/**
 * Domain repository port for device refresh sessions.
 *
 * @author Joel Huamani Estefanero
 */
public interface RefreshSessionRepository {

    RefreshSession save(RefreshSession session);

    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
