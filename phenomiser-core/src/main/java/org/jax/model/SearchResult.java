package org.jax.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

@Deprecated
public class SearchResult implements Comparable<SearchResult> {
    private final TermId tid;
    private final double pval;
    private final double similarityScore;


    public SearchResult(TermId t,double p, double s) {
        tid=t;
        this.pval=p;
        this.similarityScore=s;
    }


    @Override
    public int compareTo(SearchResult r) {
        if (pval==r.pval) {
            return Double.compare(this.similarityScore,r.similarityScore);
        } else {
            return Double.compare(r.pval,this.pval);
        }
    }
}
