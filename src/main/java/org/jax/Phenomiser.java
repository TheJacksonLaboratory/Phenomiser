package org.jax;

import org.jax.services.AbstractResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.PValue;


import java.util.*;
import java.util.stream.Collectors;

import org.monarchinitiative.phenol.stats.BenjaminiHochberg;

/**
 * Hello world!
 *
 */
public class Phenomiser {

    private static AbstractResources resources;

    public static void setResources(AbstractResources resources) {
        Phenomiser.resources = resources;
    }

    /**
     * query with a list of terms and diseases
     * @param queryTerms a list of HPO termIds
     * @param dbs a list of disease databases
     */
    public static Map<TermId, PValue> query(List<TermId> queryTerms, List<DiseaseDB> dbs) {

        if (queryTerms == null || dbs == null || queryTerms.isEmpty() || dbs.isEmpty()) {
            return null;
        }

        //a user might just want to select "OMIM", "OPHANET" or "MONDO" diseases
        //for each disease, calculate the similarity score with query terms
        SimilarityScoreCalculator similarityScoreCalculator = new SimilarityScoreCalculator(resources);
        Map<Integer, Double> similarityScores = similarityScoreCalculator.compute(queryTerms, dbs);

        //estimate p values for each disease
        PValueCalculator pValueCalculator = new PValueCalculator(queryTerms.size(), similarityScores, resources);

        //p value multi test correction

        BenjaminiHochberg bhFDR = new BenjaminiHochberg();
        Map<TermId, PValue> adjusted = bhFDR.adjustPValues(pValueCalculator);

//        Map<TermId, PValue> adjusted_sorted = adjusted.entrySet().stream()
//                .sorted(new Comparator<Map.Entry<TermId, PValue>>() {
//                    @Override
//                    public int compare(Map.Entry<TermId, PValue> o1, Map.Entry<TermId, PValue> o2) {
//                        if (o1.getValue().p_adjusted <= o2.getValue().p_adjusted) {
//                            return -1;
//                        } else {
//                            return 1;
//                        }
//                    }
//                })
//                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        return adjusted;
    }




}
