package org.jax;

import org.jax.services.AbstractResources;
import org.jax.services.PValueCalculator;
import org.jax.services.SimilarityScoreCalculator;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.Item2PValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.stream.Collectors;

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
    public static List<Item2PValue<TermId>> query(List<TermId> queryTerms, List<DiseaseDB> dbs) {

        if (queryTerms == null || dbs == null || queryTerms.isEmpty() || dbs.isEmpty()) {
            return null;
        }

        //a user might just want to select "OMIM", "OPHANET" or "MONDO" diseases
        //for each disease, calculate the similarity score with query terms
        SimilarityScoreCalculator similarityScoreCalculator = new SimilarityScoreCalculator(resources);
        Map<Integer, Double> similarityScores = similarityScoreCalculator.compute(queryTerms, dbs);
        similarityScores = filterOutNoAnnotationDisease(similarityScores);

        //estimate p values for each disease
        PValueCalculator pValueCalculator = new PValueCalculator(queryTerms.size(), similarityScores, resources);
        List<Item2PValue<TermId>> mylist = pValueCalculator.adjustPvals();

        //p value multi test correction

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

        return mylist;
    }

    //Remove diseases having no annotations
    private static Map<Integer, Double> filterOutNoAnnotationDisease(Map<Integer, Double> similarityScores) {
        if (noAnnotationDisease == null) {
            noAnnotationDisease = resources.getDiseaseIdToHpoTermIds().entrySet().stream()
                    .filter(e -> e.getValue().size()==0).map(e -> e.getKey())
                    .collect(Collectors.toSet());
        }

        if (!noAnnotationDisease.isEmpty()) {
            logger.warn("Diseases having no annotations are found! About to remove them...");

            for (TermId termId : noAnnotationDisease) {
                Integer hashcode = termId.hashCode();
                if (similarityScores.containsKey(hashcode)) {
                    similarityScores.remove(hashcode);
                    logger.warn("Remove: " + termId.getValue() + "\t" + resources.getDiseaseMap().
                            get(termId).getName());
                }
            }
        }

        return similarityScores;
    }




}
