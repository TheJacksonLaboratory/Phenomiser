package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimilarityScoreCalculator {

    private AbstractResources resources;

    public SimilarityScoreCalculator(AbstractResources resources) {
        this.resources = resources;
    }

    public Map<Integer, Double> compute(List<TermId> query, List<DiseaseDB> dbs) {

        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();

        Map<Integer, Double> similarityScores = new HashMap<>();
        resources.getDiseaseIndexToHpoTerms().entrySet().stream()
                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));

        return similarityScores;
    }

}
