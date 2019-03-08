package org.jax.model;

import com.google.common.math.DoubleMath;
import org.monarchinitiative.phenol.stats.Item2PValue;

public class Item2PValueAndSimilarity<T> extends Item2PValue<T> {

    private double similarityScore;

    /**
     * This constructor takes an Item for which a pvalue was calculated. It assigned both {@link #p_raw} (the
     * raw pavel) and {@link #p_adjusted} to this value (i.e., by default there is no multiple testing
     * correction. The class is designed to be used with other classes such as Bonferroni TODO (check Bonferroni)
     * to adjust the raw pvalues that are stored in {@link #p_adjusted}.
     *
     * @param item
     * @param p
     */
    public Item2PValueAndSimilarity(T item, double p) {
        super(item, p);
    }

    public Item2PValueAndSimilarity(T item, double p, double similarityScore) {
        super(item, p);
        this.similarityScore = similarityScore;
    }

    public Item2PValueAndSimilarity(Item2PValue<T> item2PValue) {
        super(item2PValue.getItem(), item2PValue.getRawPValue());
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    @Override
    public int compareTo(Item2PValue o) {
        Item2PValueAndSimilarity other = (Item2PValueAndSimilarity) o;
        final double DELTA = 0.0001;

        if (Double.compare(this.getRawPValue(), other.getRawPValue()) == 0) {
            return Double.compare(other
                    .similarityScore, this.similarityScore);
        } else {
            return Double.compare(this.getRawPValue(), other.getRawPValue());
        }
    }

}
