package com.andeva.atelier.platform.shared.domain.exceptions;

/**
 * Thrown when an entity referenced by identifier does not exist within the catalog or domain scope.
 *
 * @author Joel Huamani Estefanero
 */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entityName, Object id) {
        super("ENTITY_NOT_FOUND", String.format("Entity %s with identifier %s was not found", entityName, id));
    }
}
