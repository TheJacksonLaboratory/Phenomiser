package org.jax.model;

import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Map;
import java.util.Set;

/**
 * Class to represent the disease annotations map.
 */
public class DiseaseHpoTerms {

    private Map<HpoDisease, Set<TermId>> diseaseTermSetMap;

    public DiseaseHpoTerms(){

    }

    public Map<HpoDisease, Set<TermId>> getDiseaseTermSetMap() {
        return diseaseTermSetMap;
    }

}
