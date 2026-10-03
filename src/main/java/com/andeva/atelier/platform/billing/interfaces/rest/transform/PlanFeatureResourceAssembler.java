package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.PlanFeatureResource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Assembler transforming {@link PlanFeature} domain child entities into immutable {@link PlanFeatureResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class PlanFeatureResourceAssembler {

    /**
     * Transforms a single domain feature entity into its REST representation.
     *
     * @param feature non-null domain entity
     * @return REST resource
     */
    public PlanFeatureResource toResource(PlanFeature feature) {
        Objects.requireNonNull(feature, "PlanFeature cannot be null");
        return new PlanFeatureResource(
                feature.id().value(),
                feature.featureKey(),
                feature.featureKey(), // Name or featureKey representation
                feature.description(),
                feature.isEnabled()
        );
    }

    /**
     * Transforms a list of domain feature entities into a list of REST resources.
     *
     * @param features list of entities
     * @return unmodifiable list of resources, or empty list if input is null or empty
     */
    public List<PlanFeatureResource> toResourceList(List<PlanFeature> features) {
        if (features == null || features.isEmpty()) {
            return Collections.emptyList();
        }
        return features.stream()
                .filter(Objects::nonNull)
                .map(this::toResource)
                .toList();
    }
}
