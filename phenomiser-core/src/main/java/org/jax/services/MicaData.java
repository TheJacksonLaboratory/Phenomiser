package org.jax.services;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Collection;
import java.util.Map;

/**
 * Represents precomputed data for MICA analysis.
 *
 * @param diseaseIdToTermIds       Mapping from disease TermId to associated phenotype TermIds.
 * @param phenotypeIdToDiseaseIds  Number of diseases in which each phenotype TermId is observed.
 * @param termToIc                 Information content (IC) values for phenotype TermIds.
 */
public record MicaData(
        Map<TermId, Collection<TermId>> diseaseIdToTermIds,
        Map<TermId, Integer> phenotypeIdToDiseaseIds,
        Map<TermId, Double> termToIc) {
}

