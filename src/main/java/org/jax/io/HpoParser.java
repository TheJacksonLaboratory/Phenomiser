package org.jax.io;

import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class HpoParser {
    private String hpoPath;
    //private InputStream hpoStream;
    private HpoOntology hpoOntology;

    public HpoParser(String hpoPath) {
        this.hpoPath = hpoPath;
    }

    //public HpoParser(InputStream hpoStream) { this.hpoStream = hpoStream; }

    public void init() throws FileNotFoundException, PhenolException {
        HpOboParser parser = new HpOboParser(new File(hpoPath));
        this.hpoOntology = parser.parse();
    }

    public Ontology getHpo() {

        return this.hpoOntology;
    }

    public Map<TermId, Term> termIdMap() {
        return this.hpoOntology.getTermMap();
    }

}
