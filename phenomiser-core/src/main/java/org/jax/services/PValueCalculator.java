package org.jax.services;

import org.jax.model.Item2PValueAndSimilarity;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;


import java.util.HashMap;

import java.util.Map;


/**
 *
 */
public class PValueCalculator  {

    private final Map<Integer, ScoreDistribution> scoreDistributions;

    private final Map<TermId, Double> similarityScores;

    //private Map<Integer, TermId> diseaseIndexToDisease;

    private int queryTermCount;

    public PValueCalculator(int queryTermCount, Map<TermId, Double> similarityScores, AbstractResources resources) {
        //Above 10, score distributions are identical to 10
        this.queryTermCount = Math.min(queryTermCount, 10);
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        //this.diseaseIndexToDisease = resources.getDiseaseIndexToDisease();
    }

    public Map<TermId, Item2PValueAndSimilarity> calculatePValues() {

        Map<TermId, Item2PValueAndSimilarity> p_values = new
                HashMap<>();
        similarityScores.forEach((diseaseId, similarityScore) -> {
            if (scoreDistributions.containsKey(queryTermCount) &&
                    scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(diseaseId) != null) {
                double p = scoreDistributions.get(queryTermCount)
                        .getObjectScoreDistribution(diseaseId)
                        .estimatePValue(similarityScore);
                if(diseaseId.getValue().equals("OMIM:612642")) {
                    System.err.println("SCORE=" + scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(diseaseId));
                }

                p_values.put(diseaseId, new Item2PValueAndSimilarity(diseaseId, p, similarityScore));
            }
        });

        return p_values;
    }

}
