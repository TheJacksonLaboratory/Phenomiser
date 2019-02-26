package org.jax.services;

import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;
public class SimilarityScoreCalculator {

    private AbstractResources resources;

    public SimilarityScoreCalculator(AbstractResources resources) {
        this.resources = resources;
    }

    public Map<Integer, Double> compute(List<TermId> query, List<DiseaseDB> dbs) {

        String filter = dbs.stream().map(DiseaseDB::name).reduce((a, b) -> a + "|" + b).get();

        Map<Integer, Double> similarityScores = new HashMap<>();
        Map<Integer, Double> sortedSimilarityScores = new HashMap<>();

        resources.getDiseaseIndexToHpoTerms().entrySet().stream()
                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter))
                .forEach(e -> similarityScores.put(e.getKey(),
                        resources.getResnikSimilarity().computeScore(query, e.getValue())));

       for (Map.Entry<Integer,List<TermId>> mentry : resources.getDiseaseIndexToHpoTerms().entrySet()) {
           if (resources.getDiseaseIndexToDisease().get(mentry.getKey()).getPrefix().matches(filter) ) {
               TermId diseaseId = resources.getDiseaseIndexToDisease().get(mentry.getKey());
               double sim = resources.getResnikSimilarity().computeScore(query, mentry.getValue());
               if (diseaseId.getValue().equals("OMIM:612642")) {
                   System.err.println("OMIM:612642 sim="+sim );
               }
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

//        List<TermId> diseases=resources.getDiseaseIndexToHpoTerms().entrySet().stream()
//                .filter(e -> resources.getDiseaseIndexToDisease().get(e.getKey()).getPrefix().matches(filter)).collect(Collectors.toList());
        return similarityScores;
    }

}
