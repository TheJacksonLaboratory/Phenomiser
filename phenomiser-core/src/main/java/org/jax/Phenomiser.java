package org.jax;

import org.jax.model.Item2PValueAndSimilarity;
import org.jax.services.AbstractResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.BenjaminiHochberg;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.monarchinitiative.phenol.stats.MultipleTestingCorrection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.function.Consumer;

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

        //Calculate p value
        Map<TermId, Item2PValueAndSimilarity<TermId>> pvalues = pValueCalculator.calculatePValues();

        //multi test correction with default Benjamini Hochberg method, unless the option is overwritten by user
        List<Item2PValueAndSimilarity<TermId>> adjusted = new ArrayList<>(pvalues.values());
        mtcConsumer.accept(adjusted);

        return adjusted;
    }

}
