package org.jax.services;

import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.*;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.similarity.HpoResnikSimilarity;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase.OMIM;

public class PhenomiserResources {


    private final Ontology hpo;
    private final HpoResnikSimilarity hpoResnikSimilarity;
    private final Map<TermId, HpoDisease> diseaseIdToHpoDiseaseMap;

    private PhenomiserResources(Ontology hpo, HpoResnikSimilarity hpoResnikSimilarity, Map<TermId, HpoDisease> diseaseIdToHpoDiseaseMap) {
        this.hpo = hpo;
        this.hpoResnikSimilarity = hpoResnikSimilarity;
        this.diseaseIdToHpoDiseaseMap = diseaseIdToHpoDiseaseMap;
    }

    public static PhenomiserResources withOmim(Path hpoJsonPath, Path phenotypeHpoaPath) throws IOException {
        Ontology ontology = OntologyLoader.loadOntology(hpoJsonPath.toFile());
        Set<DiseaseDatabase> diseaseDbSet = Set.of(OMIM);
        HpoDiseases omimDiseases = loadOmimDiseases(ontology, phenotypeHpoaPath);
        Map<TermId, HpoDisease> termIdHpoDiseaseMap =
                HpoDiseaseAnnotationParser.loadDiseaseMap(phenotypeHpoaPath, ontology, diseaseDbSet);
        // Compute list of annotations and mapping from OMIM ID to term IDs.
        MicaCalculator calculator = new MicaCalculator(ontology, false);
        MicaData micaData = calculator.calculateMica(omimDiseases);
        HpoResnikSimilarity resnikSimilarity = HpoResnikSimilarity.from(ontology, micaData.termToIc());
        return new PhenomiserResources(ontology, resnikSimilarity, termIdHpoDiseaseMap);
    }


    private static HpoDiseases loadOmimDiseases(Ontology hpo, Path hpoaPath) throws IOException {
        Instant t1 = Instant.now();
        HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.of(Set.of(OMIM), true, HpoDiseaseLoaderOptions.DEFAULT_COHORT_SIZE);
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(hpo, options);
        HpoDiseases hpoDiseases = loader.load(hpoaPath);
        Instant t2 = Instant.now();
        System.out.printf("[INFO] Loaded phenotype.hpoa in %.3f seconds.\n", Duration.between(t1,t2).toMillis()/1000d);
        return hpoDiseases;
    }

    public Ontology getHpo() {
        return hpo;
    }

    public HpoResnikSimilarity getHpoResnikSimilarity() {
        return hpoResnikSimilarity;
    }

    public Map<TermId, HpoDisease> getDiseaseIdToHpoDiseaseMap() {
        return diseaseIdToHpoDiseaseMap;
    }
}
