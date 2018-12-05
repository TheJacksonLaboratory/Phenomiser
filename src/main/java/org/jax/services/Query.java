package org.jax.services;

import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Use this class to provide services for query
 */
public class Query {

    //list of termId of HPO
    private static List<TermId> termIdList;

    /**
     * Given a query set, return the ranked list of diseases by p value (ascending).
     * @param queryTermSet
     * @return
     */
    public static Map<HpoDisease, Double> query(Set<TermId> queryTermSet) {
        Map<HpoDisease, Double> queryResult = new LinkedHashMap<>();
        return queryResult;
    }

    /**
     * The similarity between a query set of terms and a reference set (i.e. disease) of terms is defined as the average maximum similarity of each query term with terms in the reference set.
     * sim(Q -> D) = ave(similarity(t in query, t in disease))
     * @param query
     * @param ref
     * @return
     */
    public double similarity(Set<TermId> query, Set<TermId> ref, boolean symmetry) {
        double score = 0.0;
        double score_sym = 0.0;

        if (symmetry) {
            return (score + score_sym) / 2;
        } else {
            return score;
        }
    }

    /**
     * Randomly sample the ontology to get a list of HPO terms of the size with the query set.
     * @param size
     * @return
     */
    public Set<TermId> sample(int size) {
        Collections.shuffle(termIdList);
        return termIdList.stream().limit(size).collect(Collectors.toSet());
    }


}
