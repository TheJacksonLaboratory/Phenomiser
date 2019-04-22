package org.jax;

import org.h2.mvstore.DataUtils;
import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.jax.utils.Ranker;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.monarchinitiative.phenol.stats.MultipleTestingCorrection;
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
    private static MultipleTestingCorrection<TermId> mtc = new BenjaminiHochberg<>();

    private static Consumer<List<? extends Item2PValue<TermId>>> mtcConsumer = l -> mtc.adjustPvals(l);

    public static void setResources(AbstractResources resources) {
        Phenomiser.resources = resources;
    }

    /**
     * A setter to change the default multiple test correction method
     * @param mtcoption
     */
    public static void setMTCclass(MultipleTestingCorrection<TermId> mtcoption) {
        mtc = mtcoption;
    }

    public static void setMtcMethod(Consumer<List<? extends Item2PValue<TermId>>> userDefinedMtc) {
        mtcConsumer = userDefinedMtc;
    }

    /**
     * Query with a list of terms and diseases
     * @param queryTerms a list of HPO termIds
     * @param dbs a list of disease databases
     */
    public static List<Item2PValueAndSimilarity<TermId>> query(List<TermId> queryTerms, List<DiseaseDB> dbs) {

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
        Map<TermId, Item2PValueAndSimilarity<TermId>> pvalues = pValueCalculator.calculatePValues();

        //multi test correction with default Benjamini Hochberg method, unless the option is overwritten by user
        List<Item2PValueAndSimilarity<TermId>> adjusted = new ArrayList<>(pvalues.values());
        mtcConsumer.accept(adjusted);

        return adjusted;
    }

    /**
     * Query in batch mode with multiple queries. This method optimizes resource usage to avoid repeated file io.
     * @param queries a list of query list.
     * @param dbs a list of disease databases
     * @return a list of disease ranking lists
     */
    public static List<List<Item2PValueAndSimilarity<TermId>>> batchQuery(List<List<TermId>> queries, List<DiseaseDB> dbs) {

        Map<Integer, Integer> listSizes = new HashMap<>(); // from first to last list, count how many Terms each list has
        for (int i = 0; i < queries.size(); i++) {
            listSizes.put(i, queries.get(i).size());
        }

        //process query lists in the order of how many terms they have
        listSizes.values().forEach(listSize -> {





        });

        throw new UnsupportedOperationException("TO implement");
    }

    /**
     * Provide a list of query terms and a disease ID, find the rank of specified disease in the disease ranking
     * @param queryTerms
     * @param targetDisease
     * @param dbs
     * @return
     */
    public static int findRank(List<TermId> queryTerms, TermId targetDisease, List<DiseaseDB> dbs){

        List<Item2PValueAndSimilarity<TermId>> result =  query(queryTerms, dbs);
        Ranker<Item2PValueAndSimilarity<TermId>> ranker = new Ranker<>(result);
        Map<Item2PValueAndSimilarity<TermId>, Integer> rankingMap = ranker.ranking();

        int rank = -1;
        for (Map.Entry<Item2PValueAndSimilarity<TermId>, Integer> entry : rankingMap.entrySet()) {
            if (entry.getKey().getItem().equals(targetDisease)) {
                rank = entry.getValue();
            }
        }

        return rank;
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
        throw new UnsupportedOperationException("TO implement");
    }

}
