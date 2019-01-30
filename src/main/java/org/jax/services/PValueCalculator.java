package org.jax.services;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.scoredist.ScoreDistribution;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.monarchinitiative.phenol.stats.PValue;

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
        this.queryTermCount = queryTermCount;
        this.similarityScores = similarityScores;
        this.scoreDistributions = resources.getScoreDistributions();
        this.diseaseIndexToDisease = resources.getDiseaseIndexToDisease();
    }

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

    public List<Item2PValue<TermId>> getPvalList() {
        ImmutableList.Builder<Item2PValue<TermId>> builder = new ImmutableList.Builder<>();
        Map<TermId, PValue> mymap = calculatePValues();
        for (TermId diseaseId : mymap.keySet() ) {
            Item2PValue<TermId> item = new Item2PValue<>(diseaseId,mymap.get(diseaseId));
            builder.add(item);
        }
        BenjaminiHochberg<TermId> bh = new BenjaminiHochberg<>();
        List<Item2PValue<TermId>> mylist = builder.build();

        bh.adjustPvals(mylist);

        return mylist;
    }


}
