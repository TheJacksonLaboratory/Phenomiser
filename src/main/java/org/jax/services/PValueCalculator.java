package org.jax.services;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.IPValueCalculation;
import org.monarchinitiative.phenol.stats.PValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PValueCalculator implements IPValueCalculation {

    private Map<Integer, ScoreDistribution> scoreDistributions;

    private Map<Integer, Double> similarityScores;

    private Map<Integer, TermId> diseaseIdHashToDisease;

    private List<TermId> query;

    public PValueCalculator(List<TermId> query, Map<Integer, Double> similarityScores, AbstractResources resources) {
        this.query = query;
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        this.diseaseIdHashToDisease = resources.getDiseaseIdHashToDisease();
    }

    @Override
    public Map<TermId, PValue> calculatePValues() {

        //Map<Integer, Double> p_values = new HashMap<>();
        Map<TermId, PValue> p_values = new HashMap<>();
        similarityScores.entrySet().stream()
                .forEach(s -> {
                    double p = scoreDistributions.get(query.size())
                            .getObjectScoreDistribution(s.getKey())
                            .estimatePValue(s.getValue());
                    PValue pValue = new PValue();
                    pValue.p = p;
                    p_values.put(diseaseIdHashToDisease.get(s.getKey()), pValue);
                });

        return p_values;
    }
}
