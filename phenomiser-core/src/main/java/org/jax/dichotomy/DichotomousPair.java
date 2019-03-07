package org.jax.dichotomy;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class DichotomousPair {

    private TermId yin;
    private TermId yang;

    /**
     * A pair of different terms that are dichotomous features, such as hyperkalemia and hypokalemia
     * @param a
     * @param b
     */
    public DichotomousPair(TermId a, TermId b) {
        if (a.compareTo(b) == 0) {

        }
        int compare = a.compareTo(b);
        switch (compare){
            case -1:
                this.yin = a;
                this.yang = b;
                break;
            case 0:
                throw new IllegalArgumentException("Identical terms are passed as dichotomous pairs");
            case 1:
                this.yin = b;
                this.yang = a;
        }
    }

    public TermId getYin() {
        return yin;
    }

    public void setYin(TermId yin) {
        this.yin = yin;
    }

    public TermId getYang() {
        return yang;
    }

    public void setYang(TermId yang) {
        this.yang = yang;
    }

    @Override
    public int hashCode() {


        return this.yin.hashCode() + 11 * this.yang.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DichotomousPair)) {
            return false;
        }
        DichotomousPair other = (DichotomousPair) obj;
        return this.yin.equals(other.yin) && this.yang.equals(other.yang);
    }

    @Override
    public String toString() {
        return "[yin]:\t" + this.yin.toString() + "\t[yang]:" + this.yang.toString();
    }
}
