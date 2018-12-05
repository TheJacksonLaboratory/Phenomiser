package org.jax.io;

import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public class HpoParser {
    private String hpoPath;
    private HpoOntology hpoOntology;

    public HpoParser(String hpoPath) {
        this.hpoPath = hpoPath;
    }

    public Ontology parse() throws FileNotFoundException, PhenolException {
        HpOboParser parser = new HpOboParser(new File(this.hpoPath));
        hpoOntology = parser.parse();
        return hpoOntology;
    }

    public Map<TermId, Term> termIdMap() {
        return hpoOntology.getTermMap();
    }

}
