package org.jax.services;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Bonferroni;
import org.monarchinitiative.phenol.stats.Item2PValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                p_values.put(diseaseIndexToDisease.get(key), p);
            }
        });

        return p_values;
    }

    public List<Item2PValue<TermId>> getPvalList() {
        ImmutableList.Builder<Item2PValue<TermId>> builder = new ImmutableList.Builder<>();
        Map<TermId, Double> mymap = calculatePValues();
        for (TermId diseaseId : mymap.keySet() ) {
            Item2PValue<TermId> item = new Item2PValue<>(diseaseId,mymap.get(diseaseId));
            builder.add(item);
        }
        BenjaminiHochberg<TermId> bh = new BenjaminiHochberg<>();
        //Bonferroni<TermId> bonf = new Bonferroni<>();
        List<Item2PValue<TermId>> mylist = builder.build();

        bh.adjustPvals(mylist);

        return mylist;
    }


}
