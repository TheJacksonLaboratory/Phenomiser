package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;


public class SimilarityScoreCalculator {

    private AbstractResources resources;

    public SimilarityScoreCalculator(AbstractResources resources) {
        this.resources = resources;
    }

    /**
     * This is preferred over above method
     * @param query
     * @param dbs
     * @return
     */
    public Map<TermId, Double> computeB(List<TermId> query, List<DiseaseDB> dbs) {
        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();
        Map<TermId, Double> similarityScores = new HashMap<>();

        resources.getDiseaseIdToHpoTermIdsNoExpansion().entrySet().stream()
                .filter(e -> e.getKey().getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));

        return similarityScores;
    }

}
