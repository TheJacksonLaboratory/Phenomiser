package org.jax;

import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * Reimplementation of Phenomiser with Java 8.
 * To use this class, pass in an AbstractResources instance and then call the query method.
 */
public class Phenomiser {

    private static final Logger logger = LoggerFactory.getLogger(Phenomiser.class);

    private static AbstractResources resources;
    private static Set<TermId> noAnnotationDisease;

    public static void setResources(AbstractResources resources) {
        Phenomiser.resources = resources;
    }

    /**
     * query with a list of terms and diseases
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
        Map<Integer, Double> similarityScores = similarityScoreCalculator.compute(queryTerms, dbs);

        //estimate p values for each disease
        PValueCalculator pValueCalculator = new PValueCalculator(queryTerms.size(), similarityScores, resources);

        //No need to call this method since the following call calls it anyway
//        Map<TermId, Item2PValueAndSimilarity<TermId>> pvalues =
//                pValueCalculator.calculatePValues();

        //multi test correction
        //call Benjamini Hochberg method
        List<Item2PValueAndSimilarity<TermId>> adjusted = pValueCalculator
                .adjustPvals(new BenjaminiHochberg<>());

//        //calculate p value and multi test correction
//        List<Item2PValue<TermId>> mylist = pValueCalculator.adjustPvals();

        return adjusted;
    }

}
