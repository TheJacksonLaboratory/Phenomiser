package org.jax.services;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimilarityScoreCalculator {

    private AbstractResources resources;
    private List<TermId> query;

    public SimilarityScoreCalculator(AbstractResources resources, List<TermId> query) {
        this.resources = resources;
        this.query = query;
    }

    public Map<Integer, Double> compute() {
        Map<Integer, Double> similarityScores =
                resources.getDiseaseIdHashToHpoTerms().entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey(),
                                e-> resources.getResnikSimilarity().computeScore(query, e.getValue())));
        return similarityScores;
    }

}
