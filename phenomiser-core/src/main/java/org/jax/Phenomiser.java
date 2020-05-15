package org.jax;

import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.CachedResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.jax.utils.Ranker;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.PValue;
import org.monarchinitiative.phenol.stats.mtc.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Reimplementation of Phenomiser with Java 8.
 * To use this class, pass in an AbstractResources instance and then call the query method.
 */
public class Phenomiser {

    private static final Logger logger = LoggerFactory.getLogger(Phenomiser.class);

    private static AbstractResources resources;
    //default MTC to Benjamini Hochberg
    //use the setter to change it
    private static MultipleTestingCorrection mtc = new BenjaminiHochberg();

    private static Consumer<List<? extends PValue>> mtcConsumer = l -> mtc.adjustPvals(l);

    public static void setResources(AbstractResources resources) {
        Phenomiser.resources = resources;
    }

    /**
     * A setter to change the default multiple test correction method
     * @param mtcoption
     */
    public static void setMTCclass(MultipleTestingCorrection mtcoption) {
        mtc = mtcoption;
    }

    public static void setMtcMethod(Consumer<List<? extends PValue>> userDefinedMtc) {
        mtcConsumer = userDefinedMtc;
    }

    /**
     * Query with a list of terms and diseases
     * @param queryTerms a list of HPO termIds
     * @param dbs a list of disease databases
     */
    public static List<Item2PValueAndSimilarity> query(List<TermId> queryTerms, List<DiseaseDB> dbs) {

        if (queryTerms == null || dbs == null || queryTerms.isEmpty() || dbs.isEmpty()) {
            return null;
        }

        //a user might just want to select "OMIM", "OPHANET" or "MONDO" diseases
        //for each disease, calculate the similarity score with query terms
        SimilarityScoreCalculator similarityScoreCalculator = new SimilarityScoreCalculator(resources);
        //Map<Integer, Double> similarityScores = similarityScoreCalculator.compute(queryTerms, dbs);
        Map<TermId, Double> similarityScores = similarityScoreCalculator.computeB(queryTerms, dbs);

        //estimate p values for each disease
        PValueCalculator pValueCalculator = new PValueCalculator(queryTerms.size(), similarityScores, resources);

        //Calculate p value
        Map<TermId, Item2PValueAndSimilarity> pvalues = pValueCalculator.calculatePValues();

        //multi test correction with default Benjamini Hochberg method, unless the option is overwritten by user
        List<Item2PValueAndSimilarity> adjusted = new ArrayList<>(pvalues.values());
        mtcConsumer.accept(adjusted);

        return adjusted;
    }



    /**
     * Provide a list of query terms and a disease ID, find the rank of specified disease in the disease ranking
     * @param queryTerms
     * @param targetDisease
     * @param dbs
     * @return
     */
    public static int findRank(List<TermId> queryTerms, TermId targetDisease, List<DiseaseDB> dbs){

        List<Item2PValueAndSimilarity> result =  query(queryTerms, dbs);
        Ranker<Item2PValueAndSimilarity> ranker = new Ranker<>(result);
        Map<Item2PValueAndSimilarity, Integer> rankingMap = ranker.ranking();

        int rank = -1;
        for (Map.Entry<Item2PValueAndSimilarity, Integer> entry : rankingMap.entrySet()) {
            if (entry.getKey().getItem().equals(targetDisease)) {
                rank = entry.getValue();
            }
        }

        return rank;
    }

    /**
     * Query in batch mode with multiple queries. This method optimizes resource usage to avoid repeated file io.
     * @param queries a list of query list.
     * @param dbs a list of disease databases
     * @return a list of disease ranking lists
     */
    public static List<List<Item2PValueAndSimilarity>> batchQuery(List<List<TermId>> queries, List<DiseaseDB> dbs) {

        // from first to last list, count how many Terms each list has
        Set<Integer> termCounts = queries.stream().map(List::size).collect(Collectors.toSet());

        List<List<Item2PValueAndSimilarity>> queryResults = new ArrayList<>();

        //process query lists in the order of how many terms they have
        termCounts.forEach(termCount -> {
            if (resources instanceof CachedResources) {
                ((CachedResources) resources).cleanAndLoadScoreDistribution(termCount);
            }

            for (int i = 0; i < queries.size(); i++) {
                if (queries.get(i).size() == termCount) {
                    List<Item2PValueAndSimilarity> queryResult = query(queries.get(i), dbs);
                    queryResults.add(i, queryResult);
                }
            }
        });

        return queryResults;
    }

    /**
     * Provide multiple query term lists. For each query list, provide a target disease in a separate list. Return the rank of specified disease for each query list.
     * @param queries
     * @param targetDiseases
     * @param dbs
     * @return
     */
    public static int[] batchFindRank(List<List<TermId>> queries,
                                      List<TermId> targetDiseases, List<DiseaseDB> dbs){

        // from first to last list, count how many Terms each list has
        Set<Integer> termCounts = queries.stream().map(List::size).collect(Collectors.toSet());

        int[] ranks = new int[queries.size()];

        //process query lists in the order of how many terms they have
        termCounts.forEach(termCount -> {
            if (resources instanceof CachedResources) {
                ((CachedResources) resources).cleanAndLoadScoreDistribution(termCount);
            }

            for (int i = 0; i < queries.size(); i++) {
                if (queries.get(i).size() == termCount) {
                    int rank = findRank(queries.get(i), targetDiseases.get(i), dbs);
                    ranks[i] = rank;
                }
            }
        });

        return ranks;
    }

}
