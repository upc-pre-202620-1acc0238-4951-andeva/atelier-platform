package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers.CustomerMembershipPersistenceAssembler;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerMembershipPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.CustomerMembershipPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA adapter implementing the CustomerMembershipRepository domain port.
 *
 * @author Adiel Sanchez Santin
 */
@Repository
public class CustomerMembershipRepositoryImpl implements CustomerMembershipRepository {

    private final CustomerMembershipPersistenceRepository membershipPersistenceRepository;

    public CustomerMembershipRepositoryImpl(CustomerMembershipPersistenceRepository membershipPersistenceRepository) {
        this.membershipPersistenceRepository = Objects.requireNonNull(membershipPersistenceRepository);
    }

    @Override
    public CustomerMembership save(CustomerMembership membership) {
        CustomerMembershipPersistenceEntity entity = CustomerMembershipPersistenceAssembler.toEntity(membership);
        CustomerMembershipPersistenceEntity saved = membershipPersistenceRepository.save(entity);
        return CustomerMembershipPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<CustomerMembership> findById(CustomerMembershipId id) {
        return membershipPersistenceRepository.findById(id.value())
                .map(CustomerMembershipPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<CustomerMembership> findByCustomerIdAndUserId(CustomerId customerId, UserId userId) {
        return membershipPersistenceRepository.findByCustomerIdAndUserId(customerId.value(), userId.value())
                .map(CustomerMembershipPersistenceAssembler::toDomain);
    }

    @Override
    public List<CustomerMembership> findByCustomerId(CustomerId customerId) {
        return membershipPersistenceRepository.findAllByCustomerId(customerId.value())
                .stream()
                .map(CustomerMembershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<CustomerMembership> findByUserIdAndStatus(UserId userId, CustomerMembershipStatus status) {
        return membershipPersistenceRepository.findAllByUserIdAndStatus(userId.value(), status)
                .stream()
                .map(CustomerMembershipPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCustomerIdAndUserIdAndStatus(CustomerId customerId, UserId userId, CustomerMembershipStatus status) {
        return membershipPersistenceRepository.existsByCustomerIdAndUserIdAndStatus(
                customerId.value(),
                userId.value(),
                status
        );
    }
}
