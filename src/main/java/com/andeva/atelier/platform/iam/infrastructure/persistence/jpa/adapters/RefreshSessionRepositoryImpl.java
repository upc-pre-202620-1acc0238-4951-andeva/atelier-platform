package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.RefreshSession;
import com.andeva.atelier.platform.iam.domain.repositories.RefreshSessionRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RefreshSessionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.RefreshSessionPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class RefreshSessionRepositoryImpl implements RefreshSessionRepository {

    private final RefreshSessionPersistenceRepository persistenceRepository;

    public RefreshSessionRepositoryImpl(RefreshSessionPersistenceRepository persistenceRepository) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
    }

    @Override
    public RefreshSession save(RefreshSession session) {
        RefreshSessionPersistenceEntity entity = new RefreshSessionPersistenceEntity();
        entity.setId(session.id());
        entity.setUserId(session.userId().value());
        entity.setTokenHash(session.tokenHash());
        entity.setIssuedAt(session.issuedAt());
        entity.setExpiresAt(session.expiresAt());
        entity.setRevokedAt(session.revokedAt());
        return toDomain(persistenceRepository.save(entity));
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(String tokenHash) {
        return persistenceRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private RefreshSession toDomain(RefreshSessionPersistenceEntity entity) {
        return new RefreshSession(entity.getId(), UserId.of(entity.getUserId()), entity.getTokenHash(),
                entity.getIssuedAt(), entity.getExpiresAt(), entity.getRevokedAt());
    }
}
