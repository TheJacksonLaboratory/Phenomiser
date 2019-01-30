package org.jax.services;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.IPValueCalculation;
import org.monarchinitiative.phenol.stats.PValue;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PValueCalculator implements IPValueCalculation {

    private Map<Integer, ScoreDistribution> scoreDistributions;

    private Map<Integer, Double> similarityScores;

    private Map<Integer, TermId> diseaseIndexToDisease;

    private int queryTermCount;

    public PValueCalculator(int queryTermCount, Map<Integer, Double> similarityScores, AbstractResources resources) {
        this.queryTermCount = queryTermCount;
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        this.diseaseIndexToDisease = resources.getDiseaseIndexToDisease();
    }

    @Override
    public Map<TermId, PValue> calculatePValues() {

        //Map<Integer, Double> p_values = new HashMap<>();
        Map<TermId, PValue> p_values = new HashMap<>();
        similarityScores.entrySet().stream()
                .forEach(s -> {
                    if (scoreDistributions.containsKey(queryTermCount) &&
                            scoreDistributions.get(queryTermCount)
                                    .getObjectScoreDistribution(s.getKey()) != null) {
                        double p = scoreDistributions.get(queryTermCount)
                                .getObjectScoreDistribution(s.getKey())
                                .estimatePValue(s.getValue());
                        PValue pValue = new PValue();
                        pValue.setRawPValue(p);
                        p_values.put(diseaseIndexToDisease.get(s.getKey()), pValue);
                    }
                });

        return p_values;
    }
}
