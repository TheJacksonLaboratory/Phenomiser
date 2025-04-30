package org.jax.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Comparator;
import java.util.Objects;


public record PhenomizerScore(
        TermId diseaseId,
        String diseaseLabel,
        double similarityScore
) implements Comparable<PhenomizerScore> {

    @Override
    public int compareTo(PhenomizerScore other) {
        return Comparator
                .comparingDouble(PhenomizerScore::similarityScore)
                .compare(this, other);
    }


    @Override
    public String toString() {
        return String.format("%s [%s]: %.2f", diseaseLabel, diseaseId.getValue(), similarityScore);
    }
}
