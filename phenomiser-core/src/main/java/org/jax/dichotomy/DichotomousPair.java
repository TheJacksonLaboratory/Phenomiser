package org.jax.dichotomy;

import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.Serializable;

public class DichotomousPair implements Serializable {

    private TermId yin;
    private TermId yang;

    /**
     * A pair of different terms that are dichotomous features, such as hyperkalemia and hypokalemia
     * @param yin
     * @param yang
     */
    public DichotomousPair(TermId yin, TermId yang) {
        if (yin.compareTo(yang) == 0) {
            throw new IllegalArgumentException("Identical termIds are passed in as a dichotomous pair");
        }
        this.yin = yin;
        this.yang = yang;
    }

    public TermId getYin() {
        return yin;
    }

    private void setYin(TermId yin) {
        this.yin = yin;
    }

    public TermId getYang() {
        return yang;
    }

    private void setYang(TermId yang) {
        this.yang = yang;
    }

    @Override
    public int hashCode() {

        if (this.yin.compareTo(this.yang) > 0) {
            TermId dummy = this.yang;
            this.yin = this.yang;
            this.yang = dummy;
        }

        return this.yin.hashCode() + 11 * this.yang.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DichotomousPair)) {
            return false;
        }
        DichotomousPair other = (DichotomousPair) obj;
        return (this.yin.equals(other.yin) && this.yang.equals(other.yang)) ||
                (this.yin.equals(other.yang) && this.yang.equals(other.yin));
    }

    @Override
    public String toString() {
        return "[yin]:\t" + this.yin.toString() + "\t[yang]:" + this.yang.toString();
    }
}
