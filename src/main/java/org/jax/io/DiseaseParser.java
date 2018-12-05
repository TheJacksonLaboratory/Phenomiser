package org.jax.io;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Map;

public class DiseaseParser {

    HpoDiseaseAnnotationParser diseaseAnnotationParser;

    public DiseaseParser(String diseaseAnnotation, HpoOntology hpoOntology) {
        diseaseAnnotationParser = new HpoDiseaseAnnotationParser(diseaseAnnotation, hpoOntology);
    }

    public Map<TermId, HpoDisease> diseaseMap() throws PhenolException {
        return diseaseAnnotationParser.parse();
    }
}
