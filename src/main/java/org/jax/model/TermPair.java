package org.jax.model;

import org.jax.Exception.PhenomiserException;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Use this class to represent a pair of terms.
 */
public class TermPair {

    private String term1;
    private String term2;

    public TermPair(String term1, String term2) {
        this.term1 = term1;
        this.term2 = term2;
    }

    public TermPair(TermId term1, TermId term2) {
        this.term1 = term1.getId();
        this.term2 = term2.getId();
    }

    public TermPair(Set<TermId> pair) throws PhenomiserException {
        if(pair.size() != 2) {
            throw new PhenomiserException();
        }
        List<TermId> l = new ArrayList<>(pair);
        this.term1 = l.get(0).getId();
        this.term2 = l.get(1).getId();
    }

    public String getTerm1() {
        return term1;
    }

    public void setTerm1(String term1) {
        this.term1 = term1;
    }

    public String getTerm2() {
        return term2;
    }

    public void setTerm2(String term2) {
        this.term2 = term2;
    }

    @Override
    public boolean equals(Object o){
        if (! (o instanceof TermPair)) {
            return false;
        }
        TermPair other = (TermPair) o;
        return ((this.term1.equals(other.term1) && this.term2.equals(other.term2)) ||
                this.term1.equals(other.term2) && this.term2.equals(other.term1));
    }

    @Override
    public int hashCode() {
        return this.term1.hashCode() + 31 * this.term2.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s,%s", this.term1, this.term2);
    }

}
