package org.jax.services;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class PValueCalculator  {

    private Map<Integer, ScoreDistribution> scoreDistributions;

    private Map<Integer, Double> similarityScores;

    private Map<Integer, TermId> diseaseIndexToDisease;

    private int queryTermCount;

    public PValueCalculator(int queryTermCount, Map<Integer, Double> similarityScores, AbstractResources resources) {
        //Above 10, score distributions are identical to 10
        this.queryTermCount = Math.min(queryTermCount, 10);
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        this.diseaseIndexToDisease = resources.getDiseaseIndexToDisease();
    }

    public Map<TermId, Double> calculatePValues() {

        Map<TermId, Double> p_values = new HashMap<>();
        similarityScores.forEach((key, value) -> {
            if (scoreDistributions.containsKey(queryTermCount) &&
                    scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(key) != null) {
                double p = scoreDistributions.get(queryTermCount)
                        .getObjectScoreDistribution(key)
                        .estimatePValue(value);
                if(diseaseIndexToDisease.get(key).getValue().equals("OMIM:612642")) {
                    System.err.println("SCORE=" + scoreDistributions.get(queryTermCount)
                            .getObjectScoreDistribution(key));
                }


                p_values.put(diseaseIndexToDisease.get(key), p);
            }
        });

        return p_values;
    }

    public List<Item2PValue<TermId>> adjustPvals() {

        Map<TermId, Double> mymap = calculatePValues();
        List<Item2PValue<TermId>> mylist = mymap.entrySet().stream().map(e -> new Item2PValue<>(e.getKey(), e.getValue())).collect(Collectors.toList());

        BenjaminiHochberg<TermId> bh = new BenjaminiHochberg<>();
        bh.adjustPvals(mylist);

        return mylist;
    }


}
