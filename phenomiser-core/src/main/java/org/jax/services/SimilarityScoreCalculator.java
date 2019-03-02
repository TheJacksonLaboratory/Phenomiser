package org.jax.services;

import org.jax.model.SearchResult;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class SimilarityScoreCalculator {

    private AbstractResources resources;

    public SimilarityScoreCalculator(AbstractResources resources) {
        this.resources = resources;
    }

    @Deprecated
    public Map<Integer, Double> compute(List<TermId> query, List<DiseaseDB> dbs) {

        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();

        Map<Integer, Double> similarityScores = new HashMap<>();

        resources.getDiseaseIndexToHpoTermsNoExpansion().entrySet().stream()
                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));

        return similarityScores;
    }

    /**
     * This is preferred over above method
     * @TODO: refactor PValueCalculator to use this method
     * @param query
     * @param dbs
     * @return
     */
    public Map<TermId, Double> computeB(List<TermId> query, List<DiseaseDB>
            dbs) {

        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();

        Map<TermId, Double> similarityScores = new HashMap<>();

        resources.getDiseaseIdToHpoTermIdsNoExpansion().entrySet().stream()
                .filter(e -> e.getKey().getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));

        return similarityScores;
    }

    public List<SearchResult> compute2(List<TermId> query, List<DiseaseDB> dbs) {

        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();

        Map<Integer, Double> similarityScores = new HashMap<>();
        Map<Integer, Double> sortedSimilarityScores = new HashMap<>();

        resources.getDiseaseIndexToHpoTermsWithExpansion().entrySet().stream()
                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));
/*
       for (Map.Entry<Integer,List<TermId>> mentry : resources.getDiseaseIndexToHpoTermsWithExpansion().entrySet()) {
           if (resources.getDiseaseIndexToDisease().get(mentry.getKey()).getPrefix().matches(filter) ) {
               TermId diseaseId = resources.getDiseaseIndexToDisease().get(mentry.getKey());
               double sim = resources.getResnikSimilarity().computeScore(query, mentry.getValue());
//               if (diseaseId.getValue().equals("OMIM:612642")) {
//                   System.err.println("OMIM:612642 sim="+sim );
//               }
           }
        }
        sortedSimilarityScores = similarityScores
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        System.out.println("map after sorting by values in descending order: "
                + sortedSimilarityScores);

        System.out.println("map before sorting: "
                + similarityScores);

//        List<TermId> diseases=resources.getDiseaseIndexToHpoTermsWithExpansion().entrySet().stream()
//                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter)).collect(Collectors.toList());
*/

        int QUERY_COUNT=5;
        List<SearchResult> resultlist = new ArrayList<>();

        int N = similarityScores.size();
        for (Integer i : similarityScores.keySet()) {
            TermId diseaseId = resources.getDiseaseIndexToDisease().get(i);
            double semsim = similarityScores.get(i);
            double p =  resources.scoreDistributions.get(QUERY_COUNT).getObjectScoreDistribution(i).estimatePValue(semsim);
            double pval_adj = Math.max(1.0,N*p); // Bonferroni
            SearchResult sresult = new SearchResult(diseaseId,pval_adj,semsim);
            resultlist.add(sresult);
        }

        Collections.sort(resultlist,Collections.reverseOrder());

        return resultlist;
    }




}
