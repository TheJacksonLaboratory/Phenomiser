package org.jax.services;

import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

public class PreCompute {

    private final HpoOntology hpoOntology;

    private final Map<HpoDisease, Set<TermId>> hpoDiseaseMap;

    private final List<TermId> termIdList;

    private final Map<TermId, Double> termFreqMap = null;


    private final static Random random = new Random();

    public PreCompute(HpoOntology hpoOntology, Map<HpoDisease, Set<TermId>> hpoDiseaseMap) {
        this.hpoOntology = hpoOntology;
        this.hpoDiseaseMap = hpoDiseaseMap;
        termIdList = new ArrayList<>(this.hpoOntology.getTermMap().keySet());
    }


    public double termFrequency(TermId query) {
        int DISEASE_COUNT = hpoDiseaseMap.size();
        long hasQuery = hpoDiseaseMap.entrySet().stream().filter(e -> e.getValue().contains(query)).count();
        return (double) hasQuery / DISEASE_COUNT;
    }

    /**
     * The information content of a term is defined as the negative natural logarithm of the frequency.
     * @param query
     * @return
     */
    public double IC(TermId query) {
        return Math.log(termFrequency(query));
    }

    /**
     * The most informative common ancestor.
     * @param term1
     * @param term2
     * @return
     */
    public TermId MICA(TermId term1, TermId term2) {
        Set<TermId> term1_ancestors = OntologyAlgorithm.getAncestorTerms(hpoOntology, term1, true);
        Set<TermId> term2_ancestors = OntologyAlgorithm.getAncestorTerms(hpoOntology, term2, true);
        Set<TermId> ancestors = new HashSet<>(term1_ancestors);
        ancestors.addAll(term2_ancestors);
        return ancestors.stream().reduce((t1, t2) -> IC(t1) > IC(t2) ? t1 : t2).get();
    }

    /**
     * The similarity between two terms is defined as the information content of the most informative common ancestor.
     * @param term1
     * @param term2
     * @return
     */
    public double similarity(TermId term1, TermId term2) {
        return IC(MICA(term1, term2));
    }




}
