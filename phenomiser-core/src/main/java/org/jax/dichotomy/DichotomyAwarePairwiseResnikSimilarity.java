package org.jax.dichotomy;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.similarity.PairwiseResnikSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.PairwiseSimilarity;
import org.monarchinitiative.phenol.ontology.similarity.PrecomputingPairwiseResnikSimilarity;

import java.util.Map;
import java.util.Set;


/**
 * This is a class to improve ResnikSimilarity after taking medically dichotomous phenotypes into consideration. Data on dichotomous pairs will come from loinc2hpoAnnotation.
 * @Author Aaron Zhang
 */
public class DichotomyAwarePairwiseResnikSimilarity implements PairwiseSimilarity {
    /** {@link Ontology} to base computations on. */
    private final Ontology ontology;

    /** {@link Map} from {@link TermId} to its information content. */
    private final Map<TermId, Double> termToIc;

    /** A {@link Set} of {@link DichotomousPair} **/
    private final Set<DichotomousPair> dichotomousPairs;

    /** Required default constructor for serialization. */
    protected DichotomyAwarePairwiseResnikSimilarity() {
        this.ontology = null;
        this.termToIc = null;
        this.dichotomousPairs = null;
    }

    /**
     * Construct new {@link PairwiseResnikSimilarity}.
     *
     * @param ontology {@link Ontology} to base computations on.
     * @param termToIc {@link Map} from{@link TermId} to its information content.
     */
    public DichotomyAwarePairwiseResnikSimilarity(Ontology ontology, Map<TermId, Double> termToIc, Set<DichotomousPair> dichotomousPairs) {
        this.ontology = ontology;
        this.termToIc = termToIc;
        this.dichotomousPairs = dichotomousPairs;
    }

    /**
     * Implementation of computing similarity score between a <code>query</code> and a <code>query
     * </code>.
     *
     * <h5>Performance Note</h5>
     *
     * <p>This method is a performance hotspot and already well optimized. Further speedup can be
     * gained through {@link PrecomputingPairwiseResnikSimilarity}.
     *
     * @param query Query {@link TermId}.
     * @param target Target {@link TermId}.
     * @return Precomputed pairwise Resnik similarity score.
     */
    private double computeScoreImpl(TermId query, TermId target) {
        final Set<TermId> queryTerms = getOntology().getAncestorTermIds(query, true);
        final Set<TermId> targetTerms = getOntology().getAncestorTermIds(target, true);

        double maxValue = 0.0;
        for (TermId termId : queryTerms) {
            if (targetTerms.contains(termId)) {
                maxValue = Double.max(maxValue, getTermToIc().get(termId));
            }
        }

        //if the terms are dichotomous, we flip signs--Aaron
        if (isDichotomous(query, target)) {
            maxValue = - maxValue;
        }

        return maxValue;
    }

    private boolean isDichotomous(TermId query, TermId target) {
        if (query.equals(target)) {
            return false;
        }
        return this.dichotomousPairs.contains(new DichotomousPair(query, target));
    }

    @Override
    public double computeScore(TermId query, TermId target) {
        return computeScoreImpl(query, target);
    }

    /** @return Underlying {@link Ontology}. */
    public Ontology getOntology() {
        return ontology;
    }

    /** @return {@link Map} from {@link TermId} to information content. */
    public Map<TermId, Double> getTermToIc() {
        return termToIc;
    }

    public Set<DichotomousPair> getDichotomousPairs() {
        return dichotomousPairs;
    }
}
